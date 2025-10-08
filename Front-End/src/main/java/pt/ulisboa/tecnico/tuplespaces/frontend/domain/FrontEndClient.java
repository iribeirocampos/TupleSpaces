package pt.ulisboa.tecnico.tuplespaces.frontend.domain;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerGrpc;
import java.util.List;
import static pt.ulisboa.tecnico.tuplespaces.frontend.Debug.debug_;
import pt.ulisboa.tecnico.tuplespaces.frontend.HeaderServerInterceptor;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers.GetTuplesObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers.ReadObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers.TakeObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers.TakeLockObserver;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers.TakeReleaseObserver;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.util.Random;
import java.util.HashMap;

// envia para o Servidor
public class FrontEndClient {
    static final Metadata.Key<String> MY_HEADER_KEY = Metadata.Key.of("delay", Metadata.ASCII_STRING_MARSHALLER);
    private final TupleServerGrpc.TupleServerStub[] stubs;
    private String[] delays;
    private ManagedChannel[] channel;
    private final HashMap<Integer, Integer> clientLastOperation = new HashMap<>(); // Tracks operations per client
    private int retries = 0;

    public FrontEndClient(String[] host_ports, int client_id) {
        this.delays = new String[host_ports.length];
        this.channel = new ManagedChannel[host_ports.length];
        this.stubs = new TupleServerGrpc.TupleServerStub[host_ports.length];
        for (int i = 0; i < host_ports.length; i++) {
            this.channel[i] = ManagedChannelBuilder.forTarget(host_ports[i]).usePlaintext().build();

        }
    }

    private synchronized void incrementOperation(int clientId) throws InterruptedException {
        clientLastOperation.put(clientId, clientLastOperation.getOrDefault(clientId, 0) + 1);
        debug_(String.format("Client %d has performed %d operations\n", clientId, clientLastOperation.get(clientId)));
        notifyAll();
    }

    private synchronized void checkLastOperation(int clientId, int lastOperation) throws InterruptedException {
        while (this.clientLastOperation.getOrDefault(clientId, 0) + 1 != lastOperation) {
            debug_("[WARNING] Client " + clientId
                    + " has not performed the last operation, trying to process operation "
                    + lastOperation + " but should be doing operation " + (clientLastOperation.getOrDefault(clientId, 0)
                            + 1)
                    + ",WAITING\n");
            wait();
        }
    }

    public void put(String tuple, int clientId, int lastOperation) throws InterruptedException, StatusRuntimeException {
        setDelays(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        checkLastOperation(clientId, lastOperation); // checks if can proceed
        new Thread(() -> {
            try {
                ResponseCollector collector = new ResponseCollector();
                PutServerRequest request = PutServerRequest.newBuilder().setNewTuple(tuple).build();
                PutObserver observer = new PutObserver(collector);
                for (int i = 0; i < this.stubs.length; i++) {
                    debug_(String.format("#PUT# -> SENDING command to server %s with delay %s\n", i, delays[i]));
                    this.stubs[i].put(request, observer);
                }
                // Start a new thread to handle the waiting and incrementing operation
                collector.waitUntilAllReceived(this.stubs.length); // Wait for all responses
                incrementOperation(clientId); // Increment operation after waiting
                debug_("#PUT# -> SUCCESS Put operation completed for client " + clientId + "\n");
            } catch (InterruptedException e) {
                debug_("[ERROR] #PUT# -> Error while waiting for responses\n");
            }
        }).start();
    }

    public String read(String searchPattern, int clientId, int lastOperation)
            throws InterruptedException, StatusRuntimeException {
        setDelays(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        checkLastOperation(clientId, lastOperation); // checks if can proceed
        ReadServerRequest request = ReadServerRequest.newBuilder().setSearchPattern(searchPattern).build();
        ResponseCollector collector = new ResponseCollector();
        ReadObserver observer = new ReadObserver(collector);
        for (int i = 0; i < this.stubs.length; i++) {
            debug_(String.format("#READ# -> SENDING command read to server %s with delay %s\n", i, delays[i]));
            this.stubs[i].read(request, observer);
        }
        collector.waitUntilAllReceived(1);
        incrementOperation(clientId);
        return observer.getResult();

    }

    private String backoff(String searchPattern, int clientId) throws InterruptedException, StatusRuntimeException {
        Random random = new Random();
        // int sleepTime = random.nextInt(4000 + 1) + 1000;
        int sleepTime = Math.min(1000 * (1 << this.retries), 16000); // Exponential backoff
        debug_("Backing off client " + clientId + ", releasing lock and trying again with a random wait of " + sleepTime
                + "\n");
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setDelays("0 0 0"); // Reseting the delays
        return aquireLock(searchPattern, clientId);
    }

    private void releaseLocks(String searchPattern, int clientId, int[] lockServers)
            throws InterruptedException, StatusRuntimeException {
        TakeReleaseRequest exitRequest = TakeReleaseRequest.newBuilder().setTuple(searchPattern).setClientId(clientId)
                .build();
        ResponseCollector collectorExit = new ResponseCollector();
        TakeReleaseObserver observerExit = new TakeReleaseObserver(collectorExit);
        for (int i : lockServers) {
            debug_(String.format("#RELEASE# -> SENDING command RELEASE to server %s \n", i));
            this.stubs[i].takeRelease(exitRequest, observerExit);
        }
    }

    private String aquireLock(String searchPattern, int clientId) throws InterruptedException, StatusRuntimeException {
        // Entry on critic section
        TakeEntryRequest entryRequest = TakeEntryRequest.newBuilder().setTuple(searchPattern)
                .setClientId(clientId)
                .build();
        ResponseCollector collectorEntry = new ResponseCollector();
        TakeLockObserver observerEntry = new TakeLockObserver(collectorEntry);
        debug_(String.format("#LOCK# -> ASKING LOCK Client id is %s, from servers %s and %s\n", clientId, clientId % 3,
                (clientId + 1) % 3));
        int[] lockServers = new int[2];
        lockServers[0] = clientId % 3;
        lockServers[1] = ((clientId + 1) % 3);
        for (int i : lockServers) {
            debug_(String.format("#LOCK# -> SENDING command TAKE LOCK to server %s with delay %s\n", i, delays[i]));
            this.stubs[i].takeEntry(entryRequest, observerEntry);
        }
        collectorEntry.waitUntilAllReceived(lockServers.length);

        String tupleToTake = observerEntry.getInterception();
        if (tupleToTake == null) {
            debug_("[WARNING] #TAKE LOCK#  - No common tuple was found in voter_set, releasing and backing off and trying again\n");
            releaseLocks(searchPattern, clientId, lockServers);
            this.retries++;
            tupleToTake = backoff(searchPattern, clientId);
        }
        if (tupleToTake != null) {
            debug_("#TAKE LOCK# - Common values from response: " + tupleToTake + "\n");
            this.retries = 0;
            return tupleToTake;
        }
        return tupleToTake;
    }

    public String take(String searchPattern, int clientId, int lastOperation)
            throws InterruptedException, StatusRuntimeException {
        debug_(String.format("#TAKE# -> INITIALIZING take Process for tuple %s by client id %s\n", searchPattern,
                clientId));
        setDelays(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        checkLastOperation(clientId, lastOperation); // checks if can proceed

        // Acquire the lock
        String tupleToTake = aquireLock(searchPattern, clientId);
        new Thread(() -> {
            try {
                // Taking the tuple
                debug_("#LOCK# -> SUCCESS lock acquired for tuple " + tupleToTake + ", going for actual take for client"
                        + clientId + "\n");
                TakeServerRequest request = TakeServerRequest.newBuilder().setSearchPattern(tupleToTake)
                        .setClientId(clientId)
                        .build();
                ResponseCollector collector = new ResponseCollector();
                TakeObserver observer = new TakeObserver(collector);
                for (int i = 0; i < this.stubs.length; i++) {
                    debug_(String.format("#TAKE# -> SENDING command TAKE to server %s\n", i));
                    this.stubs[i].take(request, observer);
                }
                collector.waitUntilAllReceived(this.stubs.length);
                incrementOperation(clientId);
            } catch (InterruptedException e) {
                debug_("[ERROR] #TAKE# -> Error while waiting for responses\n");
            }

        }).start();
        return tupleToTake;
    }

    public List<String> getTupleSpacesState(int clientId, int lastOperation)
            throws InterruptedException, StatusRuntimeException {
        setDelays(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        checkLastOperation(clientId, lastOperation);
        getTupleSpacesStateServerRequest request = getTupleSpacesStateServerRequest.newBuilder().build();
        ResponseCollector collector = new ResponseCollector();
        GetTuplesObserver observer = new GetTuplesObserver(collector);
        for (int i = 0; i < this.stubs.length; i++) {
            debug_(String.format("#TUPLES# -> SENDING command getTupleSpacesState to server %s with delay %s\n", i,
                    delays[i]));
            this.stubs[i].getTupleSpacesState(request, observer);
        }
        collector.waitUntilAllReceived(this.stubs.length);
        incrementOperation(clientId);
        return observer.getResult();

    }

    public void setDelays(String delays_) {
        this.delays = delays_.split(" ");
        for (int i = 0; i < delays.length; i++) {
            // Update the stubs with the new delays
            TupleServerGrpc.TupleServerStub stub = TupleServerGrpc.newStub(this.channel[i]);
            Metadata metadata = new Metadata();
            metadata.put(MY_HEADER_KEY, delays[i]);
            this.stubs[i] = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
        }
    }

}

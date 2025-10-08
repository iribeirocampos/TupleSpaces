package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import static pt.ulisboa.tecnico.tuplespaces.client.Debug.debug_;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

public class ClientService {
    static final Metadata.Key<String> MY_HEADER_KEY = Metadata.Key.of("delays", Metadata.ASCII_STRING_MARSHALLER);
    private TupleSpacesGrpc.TupleSpacesBlockingStub stubWithHeaders;
    private final ManagedChannel channel;
    private final int client_id;

    public ClientService(String host_port, int client_id) {
        debug_(String.format("ClientService created with port:%s for client_id: %d\n", host_port, client_id));
        this.channel = ManagedChannelBuilder.forTarget(host_port).usePlaintext().build();
        this.client_id = client_id;
    }

    public String put(String tuple, String[] split) {
        setDelays(String.join(" ", trimDelays(split, 2)));
        try {
            PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).setClientId(this.client_id).build();
            this.stubWithHeaders.put(request);
            return "ok";
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            return null;
        }
    }

    public String read(String searchPattern, String[] split) {
        setDelays(String.join(" ", trimDelays(split, 2)));
        try {
            ReadRequest request = ReadRequest.newBuilder().setSearchPattern(searchPattern).setClientId(this.client_id)
                    .build();
            ReadResponse response = this.stubWithHeaders.read(request);
            return response.getResult();
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            return null;
        }
    }

    public String take(String searchPattern, String[] split) {
        setDelays(String.join(" ", trimDelays(split, 2)));
        try {
            TakeRequest request = TakeRequest.newBuilder().setSearchPattern(searchPattern).setClientId(this.client_id)
                    .build();
            TakeResponse response = this.stubWithHeaders.take(request);
            return response.getResult();
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            return null;
        }
    }

    public List<String> getTupleSpacesState() {
        setDelays("0 0 0");
        try {
            getTupleSpacesStateRequest request = getTupleSpacesStateRequest.newBuilder().build();
            getTupleSpacesStateResponse response = this.stubWithHeaders.getTupleSpacesState(request);
            return response.getTupleList();
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            return null;
        }
    }

    public void closeConnection() {
        this.channel.shutdown();
    }

    private String[] trimDelays(String[] delays, int trimSize) {
        String[] trimmedDelays;
        if (delays.length <= trimSize) {
            debug_("No delays where passed in command, setting all to 0\n");
            trimmedDelays = new String[3];
            for (int i = 0; i < 3; i++) {
                trimmedDelays[i] = "0";
            }
            return trimmedDelays;
        }
        trimmedDelays = new String[delays.length - trimSize];
        for (int i = trimSize; i < delays.length; i++) {
            trimmedDelays[i - trimSize] = delays[i];
        }
        return trimmedDelays;
    }

    private void setDelays(String delays) {
        debug_(String.format("Setting delays to: %s\n", delays));
        Metadata metadata = new Metadata();
        metadata.put(MY_HEADER_KEY, delays);
        TupleSpacesGrpc.TupleSpacesBlockingStub stub = TupleSpacesGrpc.newBlockingStub(this.channel);
        this.stubWithHeaders = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }
}

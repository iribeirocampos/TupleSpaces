package pt.ulisboa.tecnico.tuplespaces.frontend;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.*;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.FrontEndClient;

import io.grpc.StatusRuntimeException;
import java.util.List;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import static pt.ulisboa.tecnico.tuplespaces.frontend.Debug.debug_;

// Recebe dos clientes
public class FrontEndImpl extends TupleSpacesGrpc.TupleSpacesImplBase {
    private final FrontEndClient functions;
    private final HashMap<Integer, Integer> clientOperations = new HashMap<>(); // Tracks operations per client

    public FrontEndImpl(String[] host_ports, int client_id) {
        this.functions = new FrontEndClient(host_ports, client_id);
    }

    private synchronized void incrementClientOperations(int clientId) {
        clientOperations.put(clientId, clientOperations.getOrDefault(clientId, 0) + 1);
        debug_(String.format("Client %d has performed %d operations\n", clientId, clientOperations.get(clientId)));
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        debug_(String.format("(PUT) -> RECEIVED command put from client %s\n", request.getClientId()));
        try {
            incrementClientOperations(request.getClientId());
            functions.put(request.getNewTuple(), request.getClientId(),
                    clientOperations.getOrDefault(request.getClientId(), 0));
            responseObserver.onNext(PutResponse.newBuilder().build());
            responseObserver.onCompleted();
            debug_("(PUT)-> SENDING put response to client\n");
        } catch (StatusRuntimeException | InterruptedException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        debug_(String.format("(READ) -> RECEIVED command put from client %s\n", request.getClientId()));
        try {
            incrementClientOperations(request.getClientId());
            String response = functions.read(request.getSearchPattern(), request.getClientId(),
                    clientOperations.getOrDefault(request.getClientId(), 0));
            responseObserver.onNext(ReadResponse.newBuilder().setResult(response).build());
            responseObserver.onCompleted();
            debug_("(READ) -> SENDING read response to client\n");
        } catch (StatusRuntimeException | InterruptedException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        debug_(String.format("(TAKE) -> RECEIVED command put from client %s\n", request.getClientId()));
        try {
            incrementClientOperations(request.getClientId());
            String response = functions.take(request.getSearchPattern(), (int) request.getClientId(),
                    clientOperations.getOrDefault(request.getClientId(), 0));
            responseObserver.onNext(TakeResponse.newBuilder().setResult(response).build());
            responseObserver.onCompleted();
            debug_("(TAKE) -> SENDING take response to client\n");
        } catch (StatusRuntimeException | InterruptedException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request,
            StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        debug_("(TUPLES) -> RECEIVED command put from client %s\n");
        try {
            incrementClientOperations(request.getClientId());
            List<String> response = functions.getTupleSpacesState(request.getClientId(),
                    clientOperations.getOrDefault(request.getClientId(), 0));
            responseObserver.onNext(getTupleSpacesStateResponse.newBuilder().addAllTuple(response).build());
            responseObserver.onCompleted();
            debug_("(TUPLES) -> SENDING getTupleSpacesState response to client\n");
        } catch (StatusRuntimeException | InterruptedException e) {
            responseObserver.onError(e);
        }
    }
}
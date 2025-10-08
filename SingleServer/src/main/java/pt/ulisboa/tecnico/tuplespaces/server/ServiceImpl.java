package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.*;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import java.util.List;
import io.grpc.stub.StreamObserver;
import static pt.ulisboa.tecnico.tuplespaces.server.Debug.debug_;

public class ServiceImpl extends TupleSpacesGrpc.TupleSpacesImplBase {
    private final ServerState functions = new ServerState();

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        debug_("Received put request\n");
        functions.put(request.getNewTuple());
        responseObserver.onNext(PutResponse.newBuilder().build());
        responseObserver.onCompleted();
        debug_("Sending put response to Client\n");
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        debug_("Received read request\n");
        String response = functions.read(request.getSearchPattern());
        responseObserver.onNext(ReadResponse.newBuilder().setResult(response).build());
        responseObserver.onCompleted();
        debug_("Sending read response to Client\n");
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        debug_("Received take request\n");
        String response = functions.take(request.getSearchPattern());
        responseObserver.onNext(TakeResponse.newBuilder().setResult(response).build());
        responseObserver.onCompleted();
        debug_("Sending take response to Client\n");
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request,
            StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        debug_("Received getTupleSpacesState request\n");
        List<String> response = functions.getTupleSpacesState();
        responseObserver.onNext(getTupleSpacesStateResponse.newBuilder().addAllTuple(response).build());
        responseObserver.onCompleted();
        debug_("Sending getTupleSpacesState response to Client\n");
    }
}
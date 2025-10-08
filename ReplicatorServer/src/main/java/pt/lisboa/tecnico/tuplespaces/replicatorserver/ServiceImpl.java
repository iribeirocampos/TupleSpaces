package pt.ulisboa.tecnico.tuplespaces.replicatorserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.*;
import pt.ulisboa.tecnico.tuplespaces.replicatorserver.domain.ReplicatorServerState;
import java.util.List;
import io.grpc.stub.StreamObserver;
import static pt.ulisboa.tecnico.tuplespaces.replicatorserver.Debug.debug_;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class ServiceImpl extends TupleServerGrpc.TupleServerImplBase {
    private final ReplicatorServerState functions = new ReplicatorServerState();

    @Override
    public void put(PutServerRequest request, StreamObserver<PutServerResponse> responseObserver) {
        debug_("(PUT) -> RECEIVED put request\n");
        int wait = Integer.parseInt(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        if (wait!=0){
            debug_("==Waiting for " + wait + "s==\n");
            try {
                Thread.sleep(wait * 1000);
            } catch (InterruptedException e) {
                responseObserver.onError(Status.INTERNAL.withDescription("Error while waiting").withCause(e).asRuntimeException());
            }
        }
        functions.put(request.getNewTuple());
        responseObserver.onNext(PutServerResponse.newBuilder().build());
        responseObserver.onCompleted();
        debug_("(PUT) -> SENDING put response to Client\n");
    }

    @Override
    public void read(ReadServerRequest request, StreamObserver<ReadServerResponse> responseObserver) {
        debug_("(READ) -> RECEIVED read request\n");
        int wait = Integer.parseInt(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        if (wait!=0){
            debug_("==Waiting for " + wait + "s==\n");
            try {
                Thread.sleep(wait * 1000);
            } catch (InterruptedException e) {
                responseObserver.onError(Status.INTERNAL.withDescription("Error while waiting").withCause(e).asRuntimeException());
            }
        }
        String response = functions.read(request.getSearchPattern());
        responseObserver.onNext(ReadServerResponse.newBuilder().setResult(response).build());
        responseObserver.onCompleted();
        debug_("(READ) -> SENDING read response to Client\n");
    }

    @Override
    public void takeEntry(TakeEntryRequest request, StreamObserver<TakeLockServerResponse> responseObserver) {
        debug_("(LOCK) -> RECEIVED takeEntrySection request from \n"+request.getClientId()+"\n");
        int wait = Integer.parseInt(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        if (wait!=0){
            debug_("==Waiting for " + wait + "s==\n");
            try {
                Thread.sleep(wait * 1000);
            } catch (InterruptedException e) {
                responseObserver.onError(Status.INTERNAL.withDescription("Error while waiting").withCause(e).asRuntimeException());
            }
        }
        List<String> response = functions.takeEntrySection(request.getTuple(), request.getClientId());
        responseObserver.onNext(TakeLockServerResponse.newBuilder().addAllResult(response).build());
        responseObserver.onCompleted();
        debug_("(LOCK) -> SENDING takeEntrySection response to Client - LOCK AQUIRED\n");
    }


    @Override
    public void take(TakeServerRequest request, StreamObserver<TakeServerResponse> responseObserver) {
        debug_("(TAKE) -> RECEIVED take request\n");
        String response = functions.take(request.getSearchPattern(), request.getClientId());
        responseObserver.onNext(TakeServerResponse.newBuilder().setResult(response).build());
        responseObserver.onCompleted();
        debug_("(TAKE) -> SENDING take response to Client\n");
    }

    @Override
    public void takeRelease(TakeReleaseRequest request, StreamObserver<TakeReleaseResponse> responseObserver) {
        debug_("(RELEASE) -> RECEIVED takeRelease request\n");
        functions.takeRelease(request.getTuple(), request.getClientId());
        responseObserver.onNext(TakeReleaseResponse.newBuilder().build());
        responseObserver.onCompleted();
        debug_("(RELEASE) -> SENDING takeRelease response to Client\n");
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateServerRequest request,
            StreamObserver<getTupleSpacesStateServerResponse> responseObserver) {
        debug_("(TUPLES) -> RECEIVED getTupleSpacesState request\n");
        int wait = Integer.parseInt(HeaderServerInterceptor.HEADER_VALUE_CONTEXT_KEY.get());
        if (wait!=0){
            debug_("==Waiting for " + wait + "s==\n");
            try {
                Thread.sleep(wait * 1000);
            } catch (InterruptedException e) {
                responseObserver.onError(Status.INTERNAL.withDescription("Error while waiting").withCause(e).asRuntimeException());
            }
        }
        List<String> response = functions.getTupleSpacesState();
        responseObserver.onNext(getTupleSpacesStateServerResponse.newBuilder().addAllTuple(response).build());
        responseObserver.onCompleted();
        debug_("(TUPLES) -> SENDING getTupleSpacesState response to Client\n");
    }
}
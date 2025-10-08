package pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.PutServerResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.ResponseCollector;
import io.grpc.StatusRuntimeException;

public class PutObserver implements StreamObserver<PutServerResponse> {
    private Throwable error;
    ResponseCollector collector;

    public PutObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(PutServerResponse r) {
        collector.addString("OK");
    }

    @Override
    public void onError(Throwable throwable) {
        this.error = throwable;
        throw new StatusRuntimeException(io.grpc.Status.INTERNAL.withDescription("Error forwarding put request"));
    }

    @Override
    public void onCompleted() {
    }

    public Throwable getError() {
        return error;
    }
}

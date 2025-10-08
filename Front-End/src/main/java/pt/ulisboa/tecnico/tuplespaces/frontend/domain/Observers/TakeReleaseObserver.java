package pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.TakeReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.ResponseCollector;

public class TakeReleaseObserver implements StreamObserver<TakeReleaseResponse> {
    private Throwable error;
    private String result;
    private ResponseCollector collector;

    public TakeReleaseObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(TakeReleaseResponse r) {
        if (r != null) {
            collector.addString("Ok");
        }

    }

    @Override
    public void onError(Throwable throwable) {
        this.error = throwable;
    }

    @Override
    public void onCompleted() {
    }

    public String getResult() {
        return result;
    }
}
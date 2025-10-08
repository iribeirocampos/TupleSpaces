package pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.TakeServerResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.ResponseCollector;

public class TakeObserver implements StreamObserver<TakeServerResponse> {
    private Throwable error;
    private String result;
    private ResponseCollector collector;

    public TakeObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(TakeServerResponse r) {
        if (r != null) {
            collector.addString(r.getResult());
            result = r.getResult();
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
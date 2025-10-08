package pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.ReadServerResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.ResponseCollector;

public class ReadObserver implements StreamObserver<ReadServerResponse> {
    private Throwable error;
    private String result;

    ResponseCollector collector;

    public ReadObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(ReadServerResponse r) {
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

    public Throwable getError() {
        return error;
    }

    public String getResult() {
        return result;
    }
}

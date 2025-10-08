package pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers;

import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.getTupleSpacesStateServerResponse;
import pt.ulisboa.tecnico.tuplespaces.frontend.domain.ResponseCollector;

public class GetTuplesObserver implements StreamObserver<getTupleSpacesStateServerResponse> {
    private Throwable error;
    private List<String> result = new ArrayList<String>();
    ResponseCollector collector;

    public GetTuplesObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(getTupleSpacesStateServerResponse r) {
        for (int i = 0; i < r.getTupleList().size(); i++) {
            this.result.add(r.getTupleList().get(i));
        }
        collector.addString(r.toString());
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

    public List<String> getResult() {
        return result;
    }
}

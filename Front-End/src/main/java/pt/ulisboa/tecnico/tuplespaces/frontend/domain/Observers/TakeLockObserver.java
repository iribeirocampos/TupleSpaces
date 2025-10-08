package pt.ulisboa.tecnico.tuplespaces.frontend.domain.Observers;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.frontend.domain.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleServerOuterClass.TakeLockServerResponse;

import static pt.ulisboa.tecnico.tuplespaces.frontend.Debug.debug_;

import java.util.ArrayList;
import java.util.List;

public class TakeLockObserver implements StreamObserver<TakeLockServerResponse> {
    private Throwable error;
    private List<String> result1 = new ArrayList<String>();
    private List<String> result2 = new ArrayList<String>();
    private ResponseCollector collector;
    private String interception = null;

    public TakeLockObserver(ResponseCollector c) {
        this.collector = c;
    }

    @Override
    public void onNext(TakeLockServerResponse r) {
        if (r != null) {
            if (result1.isEmpty()) {
                this.result1 = r.getResultList();
                debug_("Received first result " + result1 + "\n");
            } else {
                this.result2 = r.getResultList();
                debug_("Received second result " + result2 + "\n");
                calculateCommon();
            }
        }
        collector.addString("OK");

    }

    private void calculateCommon() {
        List<String> commonValues = new ArrayList<>(result1);
        commonValues.retainAll(result2);
        if (commonValues.size() == 0)
            this.interception = null;
        else
            this.interception = commonValues.get(0);
    }

    @Override
    public void onError(Throwable throwable) {
        this.error = throwable;
    }

    @Override
    public void onCompleted() {
    }

    public String getInterception() {
        return this.interception;
    }
}
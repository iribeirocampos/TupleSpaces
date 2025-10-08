package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import static pt.ulisboa.tecnico.tuplespaces.server.Debug.debug_;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class ServerState {

  private List<String> tuples;

  public ServerState() {
    this.tuples = new ArrayList<String>();

  }

  public synchronized void put(String tuple) {
    debug_("Processing Put Request with tuple: " + tuple + "\n");
    tuples.add(tuple);
    debug_("Tuple added. Notifying all...\n");
    notifyAll();
  }

  private String getMatchingTuple(String pattern) {
    for (String tuple : this.tuples) {
      if (tuple.matches(pattern)) {
        return tuple;
      }
    }
    return null;
  }

  public synchronized String read(String pattern) {
    debug_("Processing Read Request with pattern: " + pattern + "\n");
    while (getMatchingTuple(pattern) == null) {
      try {
        debug_("No matching tuple found. Waiting...\n");
        wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
        throw new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()));
      }
    }
    debug_("Matching tuple found. Returning...\n");
    return getMatchingTuple(pattern);
  }

  public synchronized String take(String tuple) {
    debug_("Processing Take Request with pattern: " + tuple + "\n");
    while (getMatchingTuple(tuple) == null) {
      try {
        debug_("No matching tuple found. Waiting...\n");
        wait();
      } catch (InterruptedException e) {
        throw new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()));
      }
    }
    String matchingTuple = getMatchingTuple(tuple);
    tuples.remove(matchingTuple);
    debug_("Matching tuple found. Returning...\n");
    return matchingTuple;
  }

  public synchronized List<String> getTupleSpacesState() {
    debug_("Processing getTupleSpacesState Request, returning all tuples\n");
    return this.tuples;
  }
}

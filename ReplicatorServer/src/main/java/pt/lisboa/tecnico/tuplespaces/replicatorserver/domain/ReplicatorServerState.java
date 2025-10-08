package pt.ulisboa.tecnico.tuplespaces.replicatorserver.domain;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;
import static pt.ulisboa.tecnico.tuplespaces.replicatorserver.Debug.debug_;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.Comparator;


public class ReplicatorServerState {

  private ArrayList<Tuple> tuples;

  public ReplicatorServerState() {
    this.tuples = new ArrayList<Tuple>();

  }

  public synchronized void put(String tuple) {
    debug_("#PUT# -> PROCESSING Put Request with tuple: " + tuple + "\n");
    tuples.add(new Tuple(tuple)); 
    debug_("#PUT# -> SUCCESS Tuple added. Notifying all...\n");
    notifyAll();
  }

  private String getMatchingTuple(String pattern) {
    for (Tuple tuple : this.tuples) {
      if (tuple.getTuple().matches(pattern)) {
        return tuple.getTuple();
      }
    }
    return null;
  }


  private Tuple getLockedTuple(String tuple, int clientId){
    return tuples.stream().filter(t -> t.getTuple().matches(tuple)&&t.getLock()==clientId).findFirst().orElse(null);
  }
  private Tuple getUnlockedTuple(String tuple){
    return tuples.stream().filter(t -> t.getTuple().matches(tuple) && t.getLock()== 0).findFirst().orElse(null);

  }

  public synchronized String read(String pattern) {
    debug_("#READ# -> PROCESSING Read Request with pattern: " + pattern + "\n");
    while (getMatchingTuple(pattern) == null) {
      try {
        debug_("#READ# -> FAILURE No matching tuple found. WAITING...\n");
        wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
        throw new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()));
      }
    }
    debug_("#READ# -> SUCCESS Matching tuple found. Returning...\n");
    return getMatchingTuple(pattern);
  }

  public synchronized List<String> takeEntrySection(String tuple, Integer clientId){
    debug_("#LOCK# -> PROCESSING REQUEST ENTRY in critic section for tuple: " + tuple + "\n");
    List <Tuple> matchingTuples = this.tuples.stream().filter(t -> t.getTuple().matches(tuple)).collect(Collectors.toList());
    // No tuples found - Waiting untill a tuple is added
    if (matchingTuples.size() == 0) {
      debug_("#LOCK# -> FAILURE No matching tuple found. WAITING...\n");
      try {
        wait();
      } catch (InterruptedException e) {
        throw new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()));
      }
    }
    List <Tuple> unlockedTuples = matchingTuples.stream().filter(t -> t.getLock() == 0).collect(Collectors.toList());

    debug_("#LOCK# -> LOCKING tuples that matches regex...\n");
    for (Tuple t : unlockedTuples) {
      t.lock(clientId);
      debug_("Locking tuple " + t.getTuple()+" for client "+clientId+"\n");
    }
    debug_("#LOCK# -> SUCCESS Matching tuple found. LOCKING...\n");
    return unlockedTuples.stream().map(Tuple::getTuple).collect(Collectors.toList()); // returning the list of tuples that were unlocked
  }

  public synchronized String take(String tuple, int clientId) {
    debug_("#TAKE# -> PROCESSING TAKE tuple --> " + tuple + "\n");
    Tuple matchingTuple = this.tuples.stream().filter(t -> t.getTuple().matches(tuple)).findFirst().orElse(null);
    tuples.remove(matchingTuple);
    debug_("#TAKE# -> SUCCESS Tuple REMOVED\n");
    takeRelease(tuple, clientId);
    return matchingTuple.getTuple();
  }

  public synchronized void takeRelease(String tuple, Integer clientId) {
    debug_("#RELEASE# -> PROCESSING RELEASE of tuple " + tuple + "\n");
    List<Tuple> matchingTuples = this.tuples.stream()
        .filter(t -> t.getLock() == clientId)
        .collect(Collectors.toList());
    for (Tuple t : matchingTuples) {
      System.out.println("Tuple: "+t+" Client: "+clientId+"Lock"+t.getLock());
      if (t.getLock() == clientId) {
        t.unlock();
        debug_("#RELEASE# -> SUCCESS Tuple UNLOCKED\n");
      }
      else{
        debug_("#RELEASE# -> FAILURE Tuple NOT UNLOCKED by client\n");
      }
    }
    debug_("#RELEASE# - SUCCESS Tuple UNLOCKED. Notifying all...\n");
    notifyAll();
  }

  public synchronized List<String> getTupleSpacesState() {
    debug_("#TUPLES# -> PROCESSING getTupleSpacesState Request, returning all tuples\n");
    printTuples();
    return this.tuples.stream().map(Tuple::getTuple).collect(Collectors.toList());
  }
  public synchronized void printTuples() {
        for (Tuple tuple : tuples) {
            System.out.print(tuple.getTuple());
        }
    }
}

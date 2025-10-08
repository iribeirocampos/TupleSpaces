package pt.ulisboa.tecnico.tuplespaces.replicatorserver.domain;


public class Tuple {
    private String tuple;
    private Integer locked = 0; // 0 = unlocked, 0 != locked with the client id

    public Tuple(String tuple) {
        this.tuple = tuple;
    }

    public void lock(Integer clientId) {
        this.locked = clientId;
    }

    public Integer getLock() {
        return this.locked;
    }

    public void unlock() {
        this.locked = 0;
    }
    public boolean isLocked() {
        return this.locked != 0;
    }
    public String getTuple() {
        return this.tuple;
    }
}

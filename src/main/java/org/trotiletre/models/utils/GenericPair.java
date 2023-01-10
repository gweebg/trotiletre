package org.trotiletre.models.utils;

public class GenericPair<F, S> {
    private F first; //first member of pair
    private S second; //second member of pair

    public GenericPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }
}

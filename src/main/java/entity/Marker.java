package entity;

public class Marker<A, B> {
    public final A first;
    public final B second;

    public Marker(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
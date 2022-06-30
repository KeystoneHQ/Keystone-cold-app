package com.keystone.cold.util;


import java.util.Objects;

public class Tuple<F, S, T> {

    public final F first;
    public final S second;
    public final T third;


    public Tuple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple)) {
            return false;
        }
        Tuple<?, ?, ?> t = (Tuple<?, ?, ?>) o;
        return Objects.equals(t.first, first) && Objects.equals(t.second, second) && Objects.equals(t.third, third);
    }


    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
    }

    @Override
    public String toString() {
        return "Tuple{" + String.valueOf(first) + " " + String.valueOf(second) + " " + String.valueOf(third) + "}";
    }


    public static <A, B, C> Tuple<A, B, C> create(A a, B b, C c) {
        return new Tuple<A, B, C>(a, b, c);
    }
}

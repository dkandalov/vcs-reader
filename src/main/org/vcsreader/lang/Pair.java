package org.vcsreader.lang;

import org.jetbrains.annotations.NotNull;

/**
 * Stripped down version of com.intellij.openapi.util.Pair
 */
public class Pair<A, B> {
    @SuppressWarnings("unchecked")
    private static final Pair EMPTY = pair(null, null);

    public final A first;
    public final B second;

    @NotNull public static <A, B> Pair<A, B> pair(A first, B second) {
        return new Pair<A, B>(first, second);
    }

    @SuppressWarnings("unchecked")
    public static <A, B> Pair<A, B> empty() {
        return EMPTY;
    }

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "<" + first + "," + second + ">";
    }
}

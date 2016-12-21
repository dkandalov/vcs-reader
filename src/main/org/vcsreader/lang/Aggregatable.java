package org.vcsreader.lang;

public interface Aggregatable<T extends Aggregatable<T>> {
	T aggregateWith(T value);
}

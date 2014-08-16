package vcsreader.lang;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Async<T> {
    private final CountDownLatch expectedUpdates;
    private final AtomicReference<T> resultReference = new AtomicReference<T>();
    private final CopyOnWriteArrayList<Exception> exceptions = new CopyOnWriteArrayList<Exception>();
    private AsyncResultListener<T> whenReadyCallback;

    public Async() {
        this(1);
    }

    public Async(int expectedUpdates) {
        this.expectedUpdates = new CountDownLatch(expectedUpdates);
    }

    public void whenCompleted(AsyncResultListener<T> listener) {
        this.whenReadyCallback = listener;
    }

    public Async<T> completeWith(T result) {
        resultReference.set(result);
        expectedUpdates.countDown();
        if (expectedUpdates.getCount() == 0 && whenReadyCallback != null) {
            whenReadyCallback.onComplete(result);
        }
        return this;
    }

    public Async<T> completeWithFailure(Exception e) {
        exceptions.add(e);
        expectedUpdates.countDown();
        if (expectedUpdates.getCount() == 0 && whenReadyCallback != null) {
            whenReadyCallback.onComplete(resultReference.get());
        }
        return this;
    }

    public T awaitCompletion() {
        try {
            expectedUpdates.await();
        } catch (InterruptedException ignored) {
        }
        return resultReference.get();
    }

    public boolean isComplete() {
        return expectedUpdates.getCount() == 0;
    }

    public boolean hasException() {
        return !exceptions.isEmpty();
    }

    public List<Exception> exceptions() {
        return exceptions;
    }
}

package vcsreader.lang;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Async<T> {
    private final CountDownLatch expectedUpdates;
    private final AtomicReference<T> resultReference = new AtomicReference<T>();
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
}

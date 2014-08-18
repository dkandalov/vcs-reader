package vcsreader.lang;

import java.util.ArrayList;
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
            whenReadyCallback.onComplete(result, exceptions);
        }
        return this;
    }

    public Async<T> completeWithFailure(List<Exception> exceptions) {
        this.exceptions.addAll(exceptions);
        expectedUpdates.countDown();
        if (expectedUpdates.getCount() == 0 && whenReadyCallback != null) {
            whenReadyCallback.onComplete(resultReference.get(), exceptions);
        }
        return this;
    }

    public Async<T> awaitCompletion() {
        try {
            expectedUpdates.await();
        } catch (InterruptedException ignored) {
        }
        return this;
    }

    public T awaitResult() {
        try {
            expectedUpdates.await();
        } catch (InterruptedException ignored) {
        }
        if (!exceptions.isEmpty()) throw new Failure(exceptions);
        return resultReference.get();
    }

    public T result() {
        return resultReference.get();
    }

    public boolean isComplete() {
        return expectedUpdates.getCount() == 0;
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }


    public static class Failure extends RuntimeException {
        public final List<Exception> exceptions;

        public Failure(List<Exception> exceptions) {
            super(exceptions.get(0));
            this.exceptions = new ArrayList<Exception>(exceptions);
        }
    }
}

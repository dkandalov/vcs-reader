package vcsreader.lang;

import java.util.List;

public interface AsyncResultListener<T> {
    void onComplete(T result, List<Exception> exceptions);
}

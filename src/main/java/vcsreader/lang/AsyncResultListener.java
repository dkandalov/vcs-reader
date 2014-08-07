package vcsreader.lang;

public interface AsyncResultListener<T> {
    void onComplete(T result);
}

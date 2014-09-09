package vcsreader.lang;

public interface VcsCommand<R> extends Described {
    R execute();
}

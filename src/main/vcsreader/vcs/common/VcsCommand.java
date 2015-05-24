package vcsreader.vcs.common;

public interface VcsCommand<R> {
    R execute();

    String describe();
}

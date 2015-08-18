package vcsreader.vcs.commandlistener;

public interface VcsCommand<R> {
    String describe();

    R execute();
}

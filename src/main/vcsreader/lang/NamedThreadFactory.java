package vcsreader.lang;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count;

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override public Thread newThread(@NotNull Runnable runnable) {
        Thread thread = new Thread(runnable, prefix + count++);
        thread.setDaemon(true);
        return thread;
    }
}

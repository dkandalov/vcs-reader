package vcsreader.lang;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
    private final String prefix;
    private int count;

    public NamedThreadFactory() {
        prefix = "CommandExecutor-";
    }

    @Override public Thread newThread(Runnable runnable) {
        return new Thread(runnable, prefix + count++);
    }
}

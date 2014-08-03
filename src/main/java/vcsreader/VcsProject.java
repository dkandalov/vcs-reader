package vcsreader;

import java.util.List;

public class VcsProject {
    private final List<VcsRoot> vcsRoots;
    private final CommandExecutor commandExecutor;

    public VcsProject(List<VcsRoot> vcsRoots, CommandExecutor commandExecutor) {
        this.vcsRoots = vcsRoots;
        this.commandExecutor = commandExecutor;
    }

    public InitResult init() {
        final InitResult initResult = new InitResult(vcsRoots.size());
        for (VcsRoot vcsRoot : vcsRoots) {
            CommandExecutor.AsyncResult asyncResult = vcsRoot.init(commandExecutor);
            asyncResult.whenReady(new Runnable() {
                @Override public void run() {
                    initResult.update();
                }
            });
        }
        return initResult;
    }

    public static class InitResult {
        private int expected;
        private boolean ready;

        public InitResult(int expected) {
            this.expected = expected;
        }

        public synchronized void update() {
            expected--;
            if (expected == 0) {
                ready = true;
            }
        }

        public synchronized boolean isReady() {
            return ready;
        }
    }
}

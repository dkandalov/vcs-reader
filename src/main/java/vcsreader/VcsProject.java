package vcsreader;

import vcsreader.vcs.GitClone;
import vcsreader.vcs.GitLog;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static vcsreader.CommandExecutor.*;

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
            AsyncResult asyncResult = vcsRoot.init(commandExecutor);
            asyncResult.whenCompleted(new AsyncResultListener() {
                @Override public void onComplete(Result result) {
                    initResult.update(result);
                }
            });
        }
        return initResult;
    }

    public LogResult log(Date from, Date to) {
        final LogResult logResult = new LogResult(vcsRoots.size());
        for (VcsRoot vcsRoot : vcsRoots) {
            AsyncResult asyncResult = vcsRoot.log(commandExecutor, from, to);
            asyncResult.whenCompleted(new AsyncResultListener() {
                @Override public void onComplete(Result result) {
                    logResult.update(result);
                }
            });
        }
        return logResult;
    }


    public static class LogResult {
        private final CountDownLatch expectedUpdates;
        private final CopyOnWriteArrayList<String> errors = new CopyOnWriteArrayList<String>();

        public LogResult(int expectedUpdates) {
            this.expectedUpdates = new CountDownLatch(expectedUpdates);
        }

        public void update(Result result) {
            if (!result.successful) {
                errors.add(((GitLog.FailedResult) result).stderr);
            }
            expectedUpdates.countDown();
        }

        public boolean isComplete() {
            return expectedUpdates.getCount() == 0;
        }

        public LogResult awaitCompletion() {
            try {
                expectedUpdates.await();
            } catch (InterruptedException ignored) {
            }
            return this;
        }

        public boolean isSuccessful() {
            return errors.isEmpty();
        }

        public List<String> errors() {
            return errors;
        }
    }

    public static class InitResult {
        private final CountDownLatch expectedUpdates;
        private final CopyOnWriteArrayList<String> errors = new CopyOnWriteArrayList<String>();

        public InitResult(int expectedUpdates) {
            this.expectedUpdates = new CountDownLatch(expectedUpdates);
        }

        private void update(Result result) {
            if (!result.successful) {
                errors.add(((GitClone.FailedResult) result).stderr);
            }
            expectedUpdates.countDown();
        }

        public boolean isComplete() {
            return expectedUpdates.getCount() == 0;
        }

        public InitResult awaitCompletion() {
            try {
                expectedUpdates.await();
            } catch (InterruptedException ignored) {
            }
            return this;
        }

        public boolean isSuccessful() {
            return errors.isEmpty();
        }

        public List<String> errors() {
            return errors;
        }
    }
}

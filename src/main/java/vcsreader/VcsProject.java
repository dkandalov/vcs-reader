package vcsreader;

import vcsreader.vcs.GitClone;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static vcsreader.CommandExecutor.AsyncResult;
import static vcsreader.CommandExecutor.AsyncResultListener;
import static vcsreader.CommandExecutor.Result;

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

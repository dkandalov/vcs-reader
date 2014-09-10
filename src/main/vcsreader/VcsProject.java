package vcsreader;

import org.jetbrains.annotations.NotNull;
import vcsreader.lang.Async;
import vcsreader.lang.VcsCommandExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;

public class VcsProject {
    private final List<VcsRoot> vcsRoots;

    public VcsProject(List<VcsRoot> vcsRoots) {
        this(vcsRoots, new VcsCommandExecutor());
    }

    public VcsProject(List<VcsRoot> vcsRoots, VcsCommandExecutor commandExecutor) {
        this.vcsRoots = vcsRoots;
        for (VcsRoot vcsRoot : vcsRoots) {
            if (vcsRoot instanceof VcsRoot.WithExecutor) {
                ((VcsRoot.WithExecutor) vcsRoot).setExecutor(commandExecutor);
            }
        }
    }

    public Async<InitResult> init() {
        final Accumulator<InitResult> accumulator = new Accumulator<InitResult>(vcsRoots.size());
        for (VcsRoot vcsRoot : vcsRoots) {
            try {
                InitResult initResult = vcsRoot.init();
                accumulator.update(initResult, new ArrayList<Exception>());
            } catch (Exception e) {
                accumulator.update(null, asList(e));
            }
        }
        return accumulator.asyncResult;
    }

    public Async<UpdateResult> update() {
        final Accumulator<UpdateResult> accumulator = new Accumulator<UpdateResult>(vcsRoots.size());
        for (VcsRoot vcsRoot : vcsRoots) {

            try {
                UpdateResult updateResult = vcsRoot.update();
                accumulator.update(updateResult, new ArrayList<Exception>());
            } catch (Exception e) {
                accumulator.update(null, asList(e));
            }
        }
        return accumulator.asyncResult;
    }

    public Async<LogResult> log(Date fromDate, Date toDate) {
        Accumulator<LogResult> accumulator = new Accumulator<LogResult>(vcsRoots.size());
        for (final VcsRoot vcsRoot : vcsRoots) {
            try {
                LogResult logResult = vcsRoot.log(fromDate, toDate);
                logResult = (logResult != null ? logResult.setVcsRoot(vcsRoot) : null);
                accumulator.update(logResult, new ArrayList<Exception>());
            } catch (Exception e) {
                accumulator.update(null, asList(e));
            }
        }
        return accumulator.asyncResult;
    }


    private interface Mergeable<T extends Mergeable<T>> {
        T mergeWith(T result);
    }

    private static class Accumulator<T extends Mergeable<T>> {
        private final Async<T> asyncResult;
        private T mergedResult;

        public Accumulator(int expectedResults) {
            this.asyncResult = new Async<T>(expectedResults);
        }

        public synchronized void update(T result, List<Exception> exceptions) {
            if (!exceptions.isEmpty()) {
                asyncResult.completeWithFailure(exceptions);
            } else {
                if (mergedResult == null) {
                    mergedResult = result;
                } else {
                    mergedResult = mergedResult.mergeWith(result);
                }
                asyncResult.completeWith(mergedResult);
            }
        }
    }


    public static class LogContentResult {
        public static LogContentResult none = new LogContentResult("", "");

        private final String text;
        private final String stderr;

        public LogContentResult(@NotNull String text, @NotNull String stderr) {
            this.text = text;
            this.stderr = stderr;
        }

        @NotNull public String text() {
            return text;
        }

        public boolean isSuccessful() {
            return stderr.isEmpty();
        }
    }

    public static class LogResult implements Mergeable<LogResult> {
        private final List<Commit> commits;
        private final List<String> errors;

        public LogResult(List<Commit> commits, List<String> errors) {
            this.commits = commits;
            this.errors = errors;
        }

        public LogResult mergeWith(LogResult result) {
            List<Commit> newCommits = new ArrayList<Commit>(commits);
            List<String> newErrors = new ArrayList<String>(errors);
            newCommits.addAll(result.commits);
            newErrors.addAll(result.errors);
            return new LogResult(newCommits, newErrors);
        }

        public boolean isSuccessful() {
            return errors.isEmpty();
        }

        public List<String> errors() {
            return errors;
        }

        public List<Commit> getCommits() {
            List<Commit> commits = new ArrayList<Commit>(this.commits);
            sort(commits, new Comparator<Commit>() {
                @Override public int compare(@NotNull Commit commit1, @NotNull Commit commit2) {
                    return new Long(commit1.commitDate.getTime()).compareTo(commit2.commitDate.getTime());
                }
            });
            return commits;
        }

        public LogResult setVcsRoot(VcsRoot vcsRoot) {
            for (Commit commit : commits) {
                commit.setVcsRoot(vcsRoot);
            }
            return this;
        }
    }

    public static class UpdateResult implements Mergeable<UpdateResult> {
        private final List<String> errors;

        public UpdateResult() {
            this(new ArrayList<String>());
        }

        public UpdateResult(List<String> errors) {
            this.errors = errors;
        }

        @Override public UpdateResult mergeWith(UpdateResult result) {
            List<String> newErrors = new ArrayList<String>(errors);
            newErrors.addAll(result.errors);
            return new UpdateResult(newErrors);
        }

        public List<String> errors() {
            return errors;
        }

        public boolean isSuccessful() {
            return errors.isEmpty();
        }
    }

    public static class InitResult implements Mergeable<InitResult> {
        private final List<String> errors;

        public InitResult() {
            this(new ArrayList<String>());
        }

        public InitResult(List<String> errors) {
            this.errors = errors;
        }

        @Override public InitResult mergeWith(InitResult result) {
            List<String> newErrors = new ArrayList<String>(errors);
            newErrors.addAll(result.errors);
            return new InitResult(newErrors);
        }

        public List<String> errors() {
            return errors;
        }

        public boolean isSuccessful() {
            return errors.isEmpty();
        }
    }
}

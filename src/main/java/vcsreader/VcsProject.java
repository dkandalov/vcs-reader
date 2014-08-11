package vcsreader;

import org.jetbrains.annotations.NotNull;
import vcsreader.lang.Async;
import vcsreader.lang.AsyncResultListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.util.Collections.sort;
import static vcsreader.CommandExecutor.*;

public class VcsProject {
    private final List<VcsRoot> vcsRoots;
    private final CommandExecutor commandExecutor;

    public VcsProject(List<VcsRoot> vcsRoots, CommandExecutor commandExecutor) {
        this.vcsRoots = vcsRoots;
        this.commandExecutor = commandExecutor;
    }

    public Async<InitResult> init() {
        final Accumulator<InitResult> accumulator = new Accumulator<InitResult>(vcsRoots.size());
        for (VcsRoot vcsRoot : vcsRoots) {

            Async<Result> asyncResult = vcsRoot.init(commandExecutor);

            asyncResult.whenCompleted(new AsyncResultListener<Result>() {
                @Override public void onComplete(Result result) {
                    accumulator.update((InitResult) result);
                }
            });
        }
        return accumulator.asyncResult;
    }

    public Async<LogResult> log(Date fromDate, Date toDate) {
        final Accumulator<LogResult> accumulator = new Accumulator<LogResult>(vcsRoots.size());
        for (final VcsRoot vcsRoot : vcsRoots) {

            Async<Result> asyncResult = vcsRoot.log(commandExecutor, fromDate, toDate);

            asyncResult.whenCompleted(new AsyncResultListener<Result>() {
                @Override public void onComplete(Result result) {
                    accumulator.update(((LogResult) result).setVcsRoot(vcsRoot));
                }
            });
        }
        return accumulator.asyncResult;
    }


    private static class Accumulator<T extends Result> {
        private final Async<T> asyncResult;
        private Result mergedResult;

        public Accumulator(int expectedResults) {
            this.asyncResult = new Async<T>(expectedResults);
        }

        public synchronized void update(T result) {
            if (mergedResult == null) {
                mergedResult = result;
            } else {
                mergedResult = mergedResult.mergeWith(result);
            }

            //noinspection unchecked
            asyncResult.completeWith((T) mergedResult);
        }
    }

    public static class LogContentResult implements Result {
        private final String text;

        public LogContentResult(String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }

        @Override public Result mergeWith(Result result) {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public static class LogResult implements Result {
        private final List<Commit> commits;
        private final List<String> errors;

        public LogResult(List<Commit> commits, List<String> errors) {
            this.commits = commits;
            this.errors = errors;
        }

        public LogResult mergeWith(Result result) {
            LogResult logResult = (LogResult) result;
            List<Commit> newCommits = new ArrayList<Commit>(commits);
            List<String> newErrors = new ArrayList<String>(errors);
            newCommits.addAll(logResult.commits);
            newErrors.addAll(logResult.errors);
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

    public static class InitResult implements Result {
        private final List<String> errors;

        public InitResult() {
            this(new ArrayList<String>());
        }

        public InitResult(List<String> errors) {
            this.errors = errors;
        }

        @Override public Result mergeWith(Result result) {
            List<String> newErrors = new ArrayList<String>(errors);
            newErrors.addAll(((InitResult) result).errors);
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

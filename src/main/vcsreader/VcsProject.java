package vcsreader;

import org.jetbrains.annotations.NotNull;
import vcsreader.vcs.common.VcsCommandExecutor;

import java.util.*;

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

    public List<VcsRoot> vcsRoots() {
        return Collections.unmodifiableList(vcsRoots);
    }

    public CloneResult cloneToLocal() {
        Accumulator<CloneResult> accumulator = new Accumulator<CloneResult>(new CloneResult());
        for (VcsRoot vcsRoot : vcsRoots) {
            try {

                CloneResult cloneResult = vcsRoot.cloneToLocal();

                accumulator.update(cloneResult);
            } catch (Exception e) {
                accumulator.update(new CloneResult(e));
            }
        }
        return accumulator.mergedResult;
    }

    public UpdateResult update() {
        Accumulator<UpdateResult> accumulator = new Accumulator<UpdateResult>(new UpdateResult());
        for (VcsRoot vcsRoot : vcsRoots) {
            try {

                UpdateResult updateResult = vcsRoot.update();

                accumulator.update(updateResult);
            } catch (Exception e) {
                accumulator.update(new UpdateResult(e));
            }
        }
        return accumulator.mergedResult;
    }

    public LogResult log(Date fromDate, Date toDate) {
        Accumulator<LogResult> accumulator = new Accumulator<LogResult>(new LogResult());
        for (VcsRoot vcsRoot : vcsRoots) {
            try {

                LogResult logResult = vcsRoot.log(fromDate, toDate);
                logResult = (logResult != null ? logResult.setVcsRoot(vcsRoot) : null);

                accumulator.update(logResult);
            } catch (Exception e) {
                accumulator.update(new LogResult(e));
            }
        }
        return accumulator.mergedResult;
    }


    private interface Mergeable<T extends Mergeable<T>> {
        T mergeWith(T result);
    }

    private static class Accumulator<T extends Mergeable<T>> {
        private T mergedResult;

        public Accumulator(T mergedResult) {
            this.mergedResult = mergedResult;
        }

        public void update(T result) {
            if (mergedResult == null) {
                mergedResult = result;
            } else {
                mergedResult = mergedResult.mergeWith(result);
            }
        }
    }


    public static class LogContentResult {
        private final String text;
        private final String stderr;
        private final int exitCode;

        public LogContentResult(@NotNull String text) {
            this(text, "", 0);
        }

        public LogContentResult(@NotNull String stderr, int exitCode) {
            this("", stderr, exitCode);
        }

        private LogContentResult(@NotNull String text, @NotNull String stderr, int exitCode) {
            this.text = text;
            this.stderr = stderr;
            this.exitCode = exitCode;
        }

        @NotNull public String text() {
            return text;
        }

        public boolean isSuccessful() {
            return stderr.isEmpty() && exitCode == 0;
        }
    }

    public static class LogResult implements Mergeable<LogResult> {
        private final List<Commit> commits;
        private final List<String> errors;
        private final List<Exception> exceptions;

        public LogResult() {
            this(new ArrayList<Commit>(), new ArrayList<String>(), new ArrayList<Exception>());
        }

        public LogResult(Exception e) {
            this(new ArrayList<Commit>(), new ArrayList<String>(), asList(e));
        }

        public LogResult(List<Commit> commits, List<String> errors) {
            this(commits, errors, new ArrayList<Exception>());
        }

        public LogResult(List<Commit> commits, List<String> errors, List<Exception> exceptions) {
            this.commits = commits;
            this.errors = errors;
            this.exceptions = exceptions;
        }

        public LogResult mergeWith(LogResult result) {
            List<Commit> newCommits = new ArrayList<Commit>(commits);
            List<String> newErrors = new ArrayList<String>(errors);
            List<Exception> newExceptions = new ArrayList<Exception>(exceptions);
            newCommits.addAll(result.commits);
            newErrors.addAll(result.errors);
            newExceptions.addAll(result.exceptions);
            return new LogResult(newCommits, newErrors, newExceptions);
        }

        public boolean isSuccessful() {
            return errors.isEmpty() && exceptions.isEmpty();
        }

        public List<String> errors() {
            return errors;
        }

        public List<Exception> exceptions() {
            return exceptions;
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
        private final List<Exception> exceptions;

        public UpdateResult() {
            this(new ArrayList<String>(), new ArrayList<Exception>());
        }

        public UpdateResult(Exception e) {
            this(new ArrayList<String>(), asList(e));
        }

        public UpdateResult(List<String> errors, List<Exception> exceptions) {
            this.errors = errors;
            this.exceptions = exceptions;
        }

        public UpdateResult(String error) {
            this(asList(error), new ArrayList<Exception>());
        }

        @Override public UpdateResult mergeWith(UpdateResult result) {
            List<String> newErrors = new ArrayList<String>(errors);
            List<Exception> newExceptions = new ArrayList<Exception>(exceptions);
            newErrors.addAll(result.errors);
            newExceptions.addAll(result.exceptions);
            return new UpdateResult(newErrors, newExceptions);
        }

        public List<String> errors() {
            return errors;
        }

        public List<Exception> exceptions() {
            return exceptions;
        }

        public boolean isSuccessful() {
            return errors.isEmpty() && exceptions.isEmpty();
        }
    }

    public static class CloneResult implements Mergeable<CloneResult> {
        private final List<String> errors;
        private final List<Exception> exceptions;

        public CloneResult() {
            this(new ArrayList<String>(), new ArrayList<Exception>());
        }

        public CloneResult(Exception e) {
            this(new ArrayList<String>(), asList(e));
        }

        public CloneResult(List<String> errors, List<Exception> exceptions) {
            this.errors = errors;
            this.exceptions = exceptions;
        }

        public CloneResult(List<String> errors) {
            this(errors, new ArrayList<Exception>());
        }

        @Override public CloneResult mergeWith(CloneResult result) {
            List<String> newErrors = new ArrayList<String>(errors);
            List<Exception> newExceptions = new ArrayList<Exception>(exceptions);
            newErrors.addAll(result.errors);
            newExceptions.addAll(result.exceptions);
            return new CloneResult(newErrors, newExceptions);
        }

        public List<String> errors() {
            return errors;
        }

        public List<Exception> exceptions() {
            return exceptions;
        }

        public boolean isSuccessful() {
            return errors.isEmpty() && exceptions.isEmpty();
        }
    }
}

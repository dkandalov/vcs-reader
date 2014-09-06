package vcsreader.vcs.svn;

import org.jetbrains.annotations.Nullable;
import vcsreader.lang.Described;
import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;


class SvnInfo implements FunctionExecutor.Function<SvnInfo.Result>, Described {
    private final String svnPath;
    private final String repositoryUrl;

    public SvnInfo(String svnPath, String repositoryUrl) {
        this.svnPath = svnPath;
        this.repositoryUrl = repositoryUrl;
    }

    @Override public SvnInfo.Result execute() {
        ShellCommand command = svnInfo(svnPath, repositoryUrl);
        if (!command.stderr().isEmpty()) {
            return new Result("", asList(command.stdout()));
        }

        String repositoryRoot = parse(command.stdout());
        if (repositoryRoot == null) {
            return new Result("", asList("Didn't find svn root in output for " + repositoryUrl));
        } else {
            return new Result(repositoryRoot);
        }
    }

    @Nullable private static String parse(String stdout) {
        String[] lines = stdout.split("\n");
        for (String line : lines) {
            if (line.contains("Repository Root:")) {
                return line.replace("Repository Root:", "").trim();
            }
        }
        return null;
    }

    static ShellCommand svnInfo(String svnPath, String repositoryUrl) {
        return createCommand(svnPath, repositoryUrl).execute();
    }

    private static ShellCommand createCommand(String svnPath, String repositoryUrl) {
        return new ShellCommand(svnPath, "info", repositoryUrl);
    }

    @Override public String describe() {
        return createCommand(svnPath, repositoryUrl).describe();
    }

    public static class Result {
        public final String repositoryRoot;
        private final List<String> errors;

        public Result(String repositoryRoot) {
            this(repositoryRoot, new ArrayList<String>());
        }

        public Result(String repositoryRoot, List<String> errors) {
            this.repositoryRoot = repositoryRoot;
            this.errors = errors;
        }

        public List<String> errors() {
            return errors;
        }

        public boolean isSuccessful() {
            return errors.isEmpty();
        }
    }
}

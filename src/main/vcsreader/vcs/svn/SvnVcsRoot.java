package vcsreader.vcs.svn;

import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.lang.FunctionExecutor;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    private final String repositoryUrl;
    private final SvnSettings settings;
    private transient FunctionExecutor executor;

    public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public Async<InitResult> init() {
        return new Async<InitResult>().completeWith(new InitResult());
    }

    @Override public Async<UpdateResult> update() {
        return new Async<UpdateResult>().completeWith(new UpdateResult());
    }

    @Override public Async<LogResult> log(final Date fromDate, final Date toDate) {
        return executor.execute(new SvnLog(settings.svnPath, repositoryUrl, fromDate, toDate, settings.useMergeHistory));
    }

    @Override public Async<LogContentResult> contentOf(final String fileName, final String revision) {
        return executor.execute(new SvnLogFileContent(
                settings.svnPath,
                repositoryUrl,
                fileName,
                revision,
                settings.filesCharset
        ));
    }

    @Override public void setExecutor(FunctionExecutor functionExecutor) {
        this.executor = functionExecutor;
    }

    @Override public String toString() {
        return "SvnVcsRoot{" +
                "settings=" + settings +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                '}';
    }
}

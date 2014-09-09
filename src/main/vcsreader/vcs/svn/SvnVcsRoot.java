package vcsreader.vcs.svn;

import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.lang.AsyncResultListener;
import vcsreader.lang.VcsCommandExecutor;

import java.util.Date;
import java.util.List;

import static vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    private final String repositoryUrl;
    private final SvnSettings settings;
    private String repositoryRoot;
    private transient VcsCommandExecutor executor;

    public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public Async<InitResult> init() {
        final Async<InitResult> asyncResult = new Async<InitResult>();
        executor.execute(new SvnInfo(settings.svnPath, repositoryUrl))
                .whenCompleted(new AsyncResultListener<SvnInfo.Result>() {
                    @Override public void onComplete(SvnInfo.Result result, List<Exception> exceptions) {
                        if (!exceptions.isEmpty()) {
                            asyncResult.completeWithFailure(exceptions);

                        } else if (!result.isSuccessful()) {
                            asyncResult.completeWith(new InitResult(result.errors()));

                        } else {
                            repositoryRoot = result.repositoryRoot;
                            asyncResult.completeWith(new InitResult());
                        }
                    }
                });
        return asyncResult;
    }

    @Override public Async<UpdateResult> update() {
        return new Async<UpdateResult>().completeWith(new UpdateResult());
    }

    @Override public Async<LogResult> log(Date fromDate, Date toDate) {
        return executor.execute(new SvnLog(settings.svnPath, repositoryUrl, repositoryRoot, fromDate, toDate, settings.useMergeHistory));
    }

    @Override public Async<LogContentResult> contentOf(String filePath, String revision) {
        return executor.execute(new SvnLogFileContent(
                settings.svnPath,
                repositoryUrl,
                filePath,
                revision,
                settings.filesCharset
        ));
    }

    @Override public void setExecutor(VcsCommandExecutor commandExecutor) {
        this.executor = commandExecutor;
    }

    @Override public String toString() {
        return "SvnVcsRoot{" +
                "settings=" + settings +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                '}';
    }
}

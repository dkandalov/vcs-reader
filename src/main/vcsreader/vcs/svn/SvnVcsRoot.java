package vcsreader.vcs.svn;

import vcsreader.VcsRoot;
import vcsreader.lang.VcsCommandExecutor;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    private final String repositoryUrl;
    private final SvnSettings settings;
    private final AtomicReference<String> repositoryRoot = new AtomicReference<String>();
    private transient VcsCommandExecutor executor;

    public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public InitResult init() {
        SvnInfo.Result result = executor.execute(new SvnInfo(settings.svnPath, repositoryUrl));
        if (result.isSuccessful()) {
            repositoryRoot.set(result.repositoryRoot);
            return new InitResult();
        } else {
            return new InitResult(result.errors());
        }
    }

    @Override public UpdateResult update() {
        return new UpdateResult();
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        return executor.execute(new SvnLog(settings.svnPath, repositoryUrl, repositoryRoot.get(), fromDate, toDate, settings.useMergeHistory));
    }

    @Override public LogContentResult contentOf(String filePath, String revision) {
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

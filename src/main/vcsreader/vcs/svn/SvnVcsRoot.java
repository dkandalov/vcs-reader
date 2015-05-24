package vcsreader.vcs.svn;

import vcsreader.VcsRoot;
import vcsreader.vcs.common.VcsCommandExecutor;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    public final String repositoryUrl;
    public final SvnSettings settings;
    private String repositoryRoot;
    private transient VcsCommandExecutor executor;

    public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public CloneResult cloneToLocal() {
        return new CloneResult();
    }

    @Override public UpdateResult update() {
        return new UpdateResult();
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        if (repositoryRoot == null) {
            SvnInfo.Result result = executor.execute(new SvnInfo(settings.svnPath, repositoryUrl));
            repositoryRoot = result.repositoryRoot;
        }
        return executor.execute(new SvnLog(settings.svnPath, repositoryUrl, repositoryRoot, fromDate, toDate, settings.useMergeHistory));
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

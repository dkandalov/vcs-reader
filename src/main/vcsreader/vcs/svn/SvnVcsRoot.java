package vcsreader.vcs.svn;

import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.lang.FunctionExecutor;

import java.util.Date;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    private final String repositoryUrl;
    private final SvnSettings settings;
    private FunctionExecutor executor;

    public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public Async<VcsProject.InitResult> init() {
        return new Async<VcsProject.InitResult>().completeWith(new VcsProject.InitResult());
    }

    @Override public Async<VcsProject.UpdateResult> update() {
        return new Async<VcsProject.UpdateResult>().completeWith(new VcsProject.UpdateResult());
    }

    @Override public Async<VcsProject.LogResult> log(final Date fromDate, final Date toDate) {
        return executor.execute(new FunctionExecutor.Function<VcsProject.LogResult>() {
            @Override public VcsProject.LogResult execute() {
                return new SvnLog(settings.svnPath, repositoryUrl, fromDate, toDate).execute();
            }
        });
    }

    @Override public Async<VcsProject.LogContentResult> contentOf(String fileName, String revision) {
        return null;
    }

    @Override public void setExecutor(FunctionExecutor functionExecutor) {
        this.executor = functionExecutor;
    }
}

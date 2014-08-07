package vcsreader.vcs
import vcsreader.CommandExecutor
import vcsreader.VcsProject

import static vcsreader.lang.DateTimeUtil.date

class GitLog_Playground {
    static void main(String[] args) {
        def vcsRoots = [new GitVcsRoot("/tmp/junit-test", "")]
        def project = new VcsProject(vcsRoots, new CommandExecutor())

        def logResult = project.log(date("01/01/2013"), date("01/02/2013")).awaitCompletion()
        println(logResult.commits.join("\n"))
    }
}

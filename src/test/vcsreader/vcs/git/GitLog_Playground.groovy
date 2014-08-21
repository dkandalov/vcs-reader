package vcsreader.vcs.git

import vcsreader.VcsProject
import vcsreader.lang.Described
import vcsreader.lang.FunctionExecutor

import static vcsreader.lang.DateTimeUtil.date

class GitLog_Playground {
    static void main(String[] args) {
        def executor = new FunctionExecutor(new FunctionExecutor.Listener() {
            @Override void onFunctionCall(FunctionExecutor.Function function) {
                println(((Described) function).describe())
            }
        })
        def vcsRoots = [new GitVcsRoot("/tmp/junit-test", "", GitSettings.defaults())]
        def project = new VcsProject(vcsRoots, executor)

        def logResult = project.log(date("01/01/2013"), date("01/01/2014")).awaitResult()
        println(logResult.commits.join("\n"))
    }
}

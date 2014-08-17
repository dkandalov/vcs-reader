package vcsreader.vcs

import vcsreader.CommandExecutor
import vcsreader.VcsProject
import vcsreader.lang.Described

import static vcsreader.lang.DateTimeUtil.date

class GitLog_Playground {
    static void main(String[] args) {
        def commandExecutor = new CommandExecutor(new CommandExecutor.Listener() {
            @Override void onCommand(CommandExecutor.Command command) {
                println(((Described) command).describe())
            }
        })
        def vcsRoots = [new GitVcsRoot("/tmp/junit-test", "", GitSettings.defaults())]
        def project = new VcsProject(vcsRoots, commandExecutor)

        def logResult = project.log(date("01/01/2013"), date("01/01/2014")).awaitCompletion()
        println(logResult.commits.join("\n"))
    }
}

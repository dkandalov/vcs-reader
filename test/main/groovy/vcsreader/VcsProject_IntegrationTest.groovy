package vcsreader
import org.junit.Before
import org.junit.Test
import vcsreader.vcs.GitVcsRoot

import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.GitShellCommands_IntegrationTest.getPrePreparedProject
import static vcsreader.vcs.GitShellCommands_IntegrationTest.getProjectFolder

class VcsProject_IntegrationTest {
    @Test void "initialize git project"() {
        def vcsRoots = [new GitVcsRoot(projectFolder, prePreparedProject)]
        def project = new VcsProject(vcsRoots, new CommandExecutor())

        def initResult = project.init().awaitCompletion()
        assert initResult.errors() == []
        assert initResult.isSuccessful()
    }

    @Test void "log project history"() {
        def vcsRoots = [new GitVcsRoot(projectFolder, prePreparedProject)]
        def project = new VcsProject(vcsRoots, new CommandExecutor())
        project.init().awaitCompletion()

        def logResult = project.log(date("01/01/2013"), date("01/01/2023")).awaitCompletion()
        assert logResult.errors() == []
        assert logResult.isSuccessful()
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }
}

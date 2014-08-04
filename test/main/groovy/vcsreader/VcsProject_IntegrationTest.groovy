package vcsreader

import org.junit.Before
import org.junit.Test
import vcsreader.vcs.GitVcsRoot

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

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }
}

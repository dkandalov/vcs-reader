package vcsreader.vcs.hg
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import vcsreader.VcsProject

import static vcsreader.vcs.hg.HgIntegrationTestConfig.*

class HgIntegrationTest {

    @Test void "clone project"() {
        def cloneResult = project.cloneToLocal()
        assert cloneResult.vcsErrors().empty
        assert cloneResult.isSuccessful()
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }

    @BeforeClass static void setupConfig() {
        initTestConfig()
    }

	private static final String projectFolder = "/tmp/hg-commands-test/git-repo-${HgIntegrationTest.simpleName}/"

	private final hgSettings = HgSettings.defaults().withHgPath(pathToHg)
	private final vcsRoots = [new HgVcsRoot(projectFolder, referenceProject, hgSettings)]
	private final project = new VcsProject(vcsRoots)
}

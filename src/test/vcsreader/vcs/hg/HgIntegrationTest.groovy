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

	@Test void "clone project failure"() {
		def vcsRoots = [new HgVcsRoot(projectFolder, nonExistentPath, hgSettings)]
		def project = new VcsProject(vcsRoots)

		def cloneResult = project.cloneToLocal()

		assert !cloneResult.isSuccessful()
		assert cloneResult.vcsErrors().size() == 1
	}

	@Test void "update project"() {
		project.cloneToLocal()
		def updateResult = project.update()
		assert updateResult.vcsErrors().empty
		assert updateResult.isSuccessful()
	}

	@Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }

    @BeforeClass static void setupConfig() {
        initTestConfig()
    }

	private static final String projectFolder = "/tmp/hg-commands-test/hg-repo-${HgIntegrationTest.simpleName}/"

	private final hgSettings = HgSettings.defaults().withHgPath(pathToHg)
	private final vcsRoots = [new HgVcsRoot(projectFolder, referenceProject, hgSettings)]
	private final project = new VcsProject(vcsRoots)
}

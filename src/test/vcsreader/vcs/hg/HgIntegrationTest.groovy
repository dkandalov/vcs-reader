package vcsreader.vcs.hg
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import vcsreader.Change
import vcsreader.Commit
import vcsreader.VcsProject

import static vcsreader.Change.Type.NEW
import static vcsreader.Change.noRevision
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.TestUtil.assertEqualCommits
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

	@Test void "update project failure"() {
		project.cloneToLocal()
		new File(projectFolder).deleteDir()

		def updateResult = project.update()
		assert !updateResult.isSuccessful()
		assert updateResult.vcsErrors() != []
	}

	@Test void "log project history interval with no commits"() {
		project.cloneToLocal()
		def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history"() {
		project.cloneToLocal()
		def logResult = project.log(date("10/08/2014"), date("11/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(1), noRevision,
						dateTime("14:00:00 10/08/2014"),
						author,
						"initial commit",
						[new Change(NEW, "file1.txt", revision(1))]
				)
		])
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

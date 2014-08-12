package vcsreader.vcs

import org.junit.Before
import org.junit.Test
import vcsreader.Change
import vcsreader.CommandExecutor
import vcsreader.Commit
import vcsreader.VcsProject

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import static vcsreader.Change.Type.NEW
import static vcsreader.Change.noRevision
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.IntegrationTestConfig.firstRevision
import static vcsreader.vcs.IntegrationTestConfig.getAuthor
import static vcsreader.vcs.IntegrationTestConfig.prePreparedProject
import static vcsreader.vcs.IntegrationTestConfig.projectFolder
import static vcsreader.vcs.IntegrationTestConfig.secondRevision

class VcsProject_IntegrationTest {
    private final vcsRoots = [new GitVcsRoot(projectFolder, prePreparedProject, GitSettings.defaults())]
    private final project = new VcsProject(vcsRoots, new CommandExecutor())

    @Test void "initialize git project"() {
        def initResult = project.init().awaitCompletion()
        assert initResult.errors() == []
        assert initResult.isSuccessful()
    }

    @Test void "log single commit from project history"() {
        project.init().awaitCompletion()

        def logResult = project.log(date("10/08/2014"), date("11/08/2014")).awaitCompletion()

        assert logResult.commits == [
                new Commit(
                        firstRevision,
                        dateTime("14:00:00 10/08/2014"),
                        author,
                        "initial commit",
                        [new Change(NEW, "file1.txt", firstRevision, noRevision)]
                )
        ]
        assert logResult.errors() == []
        assert logResult.isSuccessful()
    }

    @Test void "log several commits from project history"() {
        project.init().awaitCompletion()

        def logResult = project.log(date("10/08/2014"), date("12/08/2014")).awaitCompletion()

        assertThat(asString(logResult.commits), equalTo(asString([
                new Commit(
                        firstRevision,
                        dateTime("14:00:00 10/08/2014"),
                        author,
                        "initial commit",
                        [new Change(NEW, "file1.txt", firstRevision, noRevision)]
                ),
                new Commit(
                        secondRevision,
                        dateTime("14:00:00 11/08/2014"),
                        author,
                        "added file2, file3",
                        [
                            new Change(NEW, "file2.txt", secondRevision, firstRevision),
                            new Change(NEW, "file3.txt", secondRevision, firstRevision)
                        ]
                )
        ])))
        assert logResult.errors() == []
        assert logResult.isSuccessful()
    }

    @Test void "log content of a file"() {
        project.init().awaitCompletion()

        def logResult = project.log(date("10/08/2014"), date("11/08/2014")).awaitCompletion()

        def change = logResult.commits.first().changes.first()
        assert change.content() == "file1 content"
        assert logResult.errors() == []
        assert logResult.isSuccessful()
    }

    private static String asString(List<Commit> commits) {
        commits.join("\n")
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }
}

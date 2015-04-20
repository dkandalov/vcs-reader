package vcsreader.vcs.svn
import org.junit.BeforeClass
import org.junit.Test
import vcsreader.Change
import vcsreader.Commit
import vcsreader.VcsProject

import static vcsreader.Change.Type.*
import static vcsreader.Change.noRevision
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.TestUtil.assertEqualCommits
import static vcsreader.vcs.TestUtil.withSortedChanges
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.*

class SvnIntegrationTest {
	private final svnSettings = SvnSettings.defaults().withSvnPath(pathToSvn)
    private final project = new VcsProject([new SvnVcsRoot(repositoryUrl, svnSettings)])

    @Test void "clone project always succeed"() {
        def vcsRoots = [new SvnVcsRoot(nonExistentUrl, svnSettings)]
        def project = new VcsProject(vcsRoots)

        def cloneResult = project.cloneToLocal()
        assert cloneResult.errors().empty
        assert cloneResult.isSuccessful()
    }

    @Test void "update project"() {
        def updateResult = project.update()
        assert updateResult.errors().empty
        assert updateResult.isSuccessful()
    }

    @Test void "log empty list of commits from project history"() {
        def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

        assert logResult.commits.empty
        assert logResult.errors().empty
        assert logResult.isSuccessful()
    }

    @Test void "log single commit from project history"() {
        def logResult = project.log(date("10/08/2014"), date("11/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(1), noRevision,
                        dateTime("15:00:00 10/08/2014"),
                        author,
                        "initial commit",
                        [new Change(NEW, "file1.txt", revision(1))]
                )
        ])
    }

    @Test void "log several commits from project history"() {
        def logResult = project.log(date("10/08/2014"), date("12/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(1), noRevision,
                        dateTime("15:00:00 10/08/2014"),
                        author,
                        "initial commit",
                        [new Change(NEW, "file1.txt", revision(1))]
                ),
                new Commit(
                        revision(2), revision(1),
                        dateTime("15:00:00 11/08/2014"),
                        author,
                        "added file2, file3",
                        [
                            new Change(NEW, "file3.txt", "", revision(2), noRevision),
                            new Change(NEW, "file2.txt", "", revision(2), noRevision)
                        ]
                )
        ])
    }

    @Test void "log modification commit"() {
        def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(3), revision(2),
                        dateTime("15:00:00 12/08/2014"),
                        author,
                        "modified file2, file3",
                        [
                            new Change(MODIFICATION, "file3.txt", "file3.txt", revision(3), revision(2)),
                            new Change(MODIFICATION, "file2.txt", "file2.txt", revision(3), revision(2))
                        ]
                )
        ])
    }

    @Test void "log moved file commit"() {
        def logResult = project.log(date("13/08/2014"), date("14/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(4), revision(3),
                        dateTime("15:00:00 13/08/2014"),
                        author,
                        "moved file1",
                        [new Change(MOVED, "folder1/file1.txt", "file1.txt", revision(4), revision(1))]
                )
        ])
    }

    @Test void "log moved and renamed file commit"() {
        def logResult = project.log(date("14/08/2014"), date("15/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(5), revision(4),
                        dateTime("15:00:00 14/08/2014"),
                        author,
                        "moved and renamed file1",
                        [new Change(MOVED, "folder2/renamed_file1.txt", "folder1/file1.txt", revision(5), revision(4)),
                         new Change(MODIFICATION, "file2.txt", "file2.txt", "5", "4")]
                )
        ])
    }

    @Test void "log deleted file"() {
        def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(6), revision(5),
                        dateTime("15:00:00 15/08/2014"),
                        author,
                        "deleted file1",
                        [new Change(DELETED, "", "folder2/renamed_file1.txt", revision(6), revision(5))]
                )
        ])
    }

    @Test void "log file with spaces and quotes in its name"() {
        def logResult = project.log(date("16/08/2014"), date("17/08/2014"))

        assertEqualCommits(logResult, [
                new Commit(
                        revision(7), revision(6),
                        dateTime("15:00:00 16/08/2014"),
                        author,
                        "added file with spaces and quotes",
                        [new Change(NEW, "\"file with spaces.txt\"", "", revision(7), noRevision)]
                )
        ])
    }

    @Test void "log content of modified file"() {
        def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

        def change = withSortedChanges(logResult.commits).first().changes.first()
        assert change.type == MODIFICATION
        assert change.content() == "file2 new content"
        assert change.contentBefore() == "file2 content"
        assert logResult.errors().empty
        assert logResult.isSuccessful()
    }

    @Test void "log content of new file"() {
        def logResult = project.log(date("11/08/2014"), date("12/08/2014"))

        def change = withSortedChanges(logResult.commits).first().changes.first()
        assert change.type == NEW
        assert change.content() == "file2 content"
        assert change.contentBefore() == ""
        assert logResult.errors().empty
        assert logResult.isSuccessful()
    }

    @Test void "log content of deleted file"() {
        def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

        def change = logResult.commits.first().changes.first()
        assert change.type == DELETED
        assert change.content() == ""
        assert change.contentBefore() == "file1 content"
        assert logResult.errors().empty
        assert logResult.isSuccessful()
    }

    @Test void "show only commits for the path when project is not root in svn repo"() {
        def nonRootProject = new VcsProject([new SvnVcsRoot(repositoryUrl + "/folder2", svnSettings)])

        def logResult = nonRootProject.log(date("01/08/2014"), date("01/09/2014"))
        assertEqualCommits(logResult, [
                new Commit(revision(5), revision(4), dateTime("15:00:00 14/08/2014"), author, "moved and renamed file1", [
                        // note that change has NEW type because it was moved from outside sub path
                        new Change(NEW, "renamed_file1.txt", "", revision(5), revision(4))
                ]),
                new Commit(revision(6), revision(5), dateTime("15:00:00 15/08/2014"), author, "deleted file1", [
                        new Change(DELETED, "", "renamed_file1.txt", revision(6), revision(5))
                ])
        ])
    }

    @Test void "content of commits in project which is not root in svn repo"() {
        def nonRootProject = new VcsProject([new SvnVcsRoot(repositoryUrl + "/folder2", svnSettings)])

        def logResult = nonRootProject.log(date("01/08/2014"), date("01/09/2014"))
        assert logResult.commits[0].comment == "moved and renamed file1"
        assert logResult.commits[0].changes[0].contentBefore() == "" // no content because file was moved from outside of project root
        assert logResult.commits[0].changes[0].content() == "file1 content"
        assert logResult.commits[1].comment == "deleted file1"
        assert logResult.commits[1].changes[0].contentBefore() == "file1 content"
        assert logResult.commits[1].changes[0].content() == ""
    }

    @Test void "get repository root"() {
        def svnInfo = new SvnInfo(pathToSvn, repositoryUrl + "/folder2")
        def result = svnInfo.execute()
        assert result.repositoryRoot == repositoryUrl
        assert result.errors().isEmpty()
    }

    @BeforeClass static void setupConfig() {
        initTestConfig()
    }
}

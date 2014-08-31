package vcsreader.vcs.svn
import org.junit.Test
import vcsreader.Change
import vcsreader.Commit

import static vcsreader.Change.Type.NEW
import static vcsreader.Change.noRevision
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.svn.SvnIntegrationTest.assertEqualCommits
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.getAuthor
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.getFirstRevision

class CommitParserTest {
    @Test void "parse commit with single change"() {
        def xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <log>
            <logentry
               revision="1">
            <author>Some Author</author>
            <date>2014-08-10T15:00:00.000000Z</date>
            <paths>
            <path
               prop-mods="false"
               text-mods="true"
               kind="file"
               action="A">/file1.txt</path>
            </paths>
            <msg>initial commit</msg>
            </logentry>
            </log>
        """.trim()

        assertEqualCommits(CommitParser.parseCommits(xml), [
                new Commit(
                        firstRevision, noRevision,
                        dateTime("14:00:00 10/08/2014"),
                        author,
                        "initial commit",
                        [new Change(NEW, "file1.txt", firstRevision)]
                )
        ])
    }
}

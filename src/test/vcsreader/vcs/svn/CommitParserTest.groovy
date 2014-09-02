package vcsreader.vcs.svn
import org.junit.Test
import vcsreader.Change
import vcsreader.Commit

import static vcsreader.Change.Type.NEW
import static vcsreader.Change.noRevision
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.svn.SvnIntegrationTest.assertEqualCommits

class CommitParserTest {
    @Test void "parse commit with single change"() {
        def xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <log>
                <logentry revision="1">
                <author>Some Author</author>
                <date>2014-08-10T15:00:00.000000Z</date>
                <paths>
                        <path prop-mods="false" text-mods="true" kind="file" action="A">/file1.txt</path>
                </paths>
                <msg>initial commit</msg>
                </logentry>
            </log>
        """.trim()

        assertEqualCommits(CommitParser.parseCommits(xml), [
                new Commit(
                        "1", noRevision,
                        dateTime("14:00:00 10/08/2014"),
                        "Some Author",
                        "initial commit",
                        [new Change(NEW, "file1.txt", "1")]
                )
        ])
    }

    @Test void "ignore commits with kind equal to 'folder'"() {
        def xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <log>
                <logentry revision="1">
                <author>Some Author</author>
                <date>2014-08-10T15:00:00.000000Z</date>
                <paths>
                    <path prop-mods="false" text-mods="true" kind="folder" action="A">/folder</path>
                    <path kind="" action="A">/file.txt</path>
                </paths>
                <msg>commit message</msg>
                </logentry>
            </log>
        """.trim()

        assertEqualCommits(CommitParser.parseCommits(xml), [
                new Commit(
                        "1", noRevision,
                        dateTime("14:00:00 10/08/2014"),
                        "Some Author",
                        "commit message",
                        [new Change(NEW, "file.txt", "1")]
                )
        ])
    }
}

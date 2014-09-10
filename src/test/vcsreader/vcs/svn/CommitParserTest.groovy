package vcsreader.vcs.svn

import org.junit.Test
import vcsreader.Change
import vcsreader.Commit

import static vcsreader.Change.Type.NEW
import static vcsreader.Change.noRevision
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.TestUtil.assertEqualCommits

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
                        dateTime("15:00:00 10/08/2014"),
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
                <date>2013-12-21T19:35:02.544940Z</date>
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
                        dateTime("19:35:02 21/12/2013"),
                        "Some Author",
                        "commit message",
                        [new Change(NEW, "file.txt", "1")]
                )
        ])
    }

    @Test void "parse commit with long message"() {
        def xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <log>
                <logentry revision="1696">
                <author>ebruneton</author>
                <date>2013-10-12T14:39:22.432589Z</date>
                <paths><path kind="" action="M">/some/file</path></paths>
<msg>- Update the product version to 5.0 beta
- Fix a few "bugs" found with Findbugs
- Switch to JDK 8 to run the tests.</msg>
</logentry>
            </log>
        """.trim()

        String comment = CommitParser.parseCommits(xml)[0].comment
        assert comment ==
                "- Update the product version to 5.0 beta\n" +
                "- Fix a few \"bugs\" found with Findbugs\n" +
                "- Switch to JDK 8 to run the tests."
    }
}
package vcsreader.vcs.infrastructure
import org.junit.Test

import static java.util.concurrent.Executors.newSingleThreadExecutor

class ShellCommandTest {
    @Test void "successful command"() {
        def shellCommand = new ShellCommand("ls").execute()
        assert shellCommand.stderr().empty
        assert !shellCommand.stdout().empty
        assert shellCommand.exitValue() == 0
    }

    @Test void "failed command"() {
        def shellCommand = new ShellCommand("fake-command").execute()
        assert shellCommand.stdout().empty
        assert !shellCommand.stderr().empty
        assert shellCommand.exitValue() == -1
    }

    @Test(timeout = 1000L) void "kill hanging command"() {
        def shellCommand = new ShellCommand("sleep", "10000")

        newSingleThreadExecutor().execute {
            Thread.sleep(200)
            shellCommand.kill()
        }
        shellCommand.execute()

        assert shellCommand.exitValue() == -1
    }
}

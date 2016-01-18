package vcsreader.lang
import org.junit.Test

import static java.util.concurrent.Executors.newSingleThreadExecutor

class ShellCommandTest {
    @Test void "successful shell command execution"() {
        def shellCommand = new ShellCommand("ls").execute()
        assert shellCommand.stderr().empty
        assert !shellCommand.stdout().empty
	    assert shellCommand.exceptionStacktrace().empty
        assert shellCommand.exitCode() == 0
    }

    @Test void "failed shell command execution"() {
        def shellCommand = new ShellCommand("fake-command").execute()
        assert shellCommand.stdout().empty
        assert shellCommand.stderr().empty
	    assert !shellCommand.exceptionStacktrace().empty
        assert shellCommand.exitCode() == -123
    }

    @Test(timeout = 1000L) void "kill hanging shell command"() {
        def shellCommand = new ShellCommand("sleep", "10000")

        newSingleThreadExecutor().execute {
            Thread.sleep(200)
            shellCommand.kill()
        }
        shellCommand.execute()

        assert shellCommand.exitCode() == -123
    }

    @Test void "shell command description"() {
        assert new ShellCommand("ls", "-l").describe() == "ls -l"
        assert new ShellCommand("ls", "-l").workingDir("/").describe() == "ls -l (working directory '/')"
    }
}

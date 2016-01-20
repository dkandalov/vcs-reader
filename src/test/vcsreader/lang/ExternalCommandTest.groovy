package vcsreader.lang

import org.junit.Test

import static java.util.concurrent.Executors.newSingleThreadExecutor

class ExternalCommandTest {
	@Test void "successful command execution"() {
		def command = new ExternalCommand("ls").execute()
		assert command.stderr().empty
		assert !command.stdout().empty
		assert command.exceptionStacktrace().empty
		assert command.exitCode() == 0
	}

	@Test void "failed command execution"() {
		def command = new ExternalCommand("fake-command").execute()
		assert command.stdout().empty
		assert command.stderr().empty
		assert !command.exceptionStacktrace().empty
		assert command.exitCode() == -123
	}

	@Test(timeout = 1000L) void "kill hanging command"() {
		def command = new ExternalCommand("sleep", "10000")

		newSingleThreadExecutor().execute {
			Thread.sleep(200)
			command.kill()
		}
		command.execute()

		assert command.exitCode() == -123
	}

	@Test void "command description"() {
		assert new ExternalCommand("ls", "-l").describe() == "ls -l"
		assert new ExternalCommand("ls", "-l").workingDir("/").describe() == "ls -l (working directory '/')"
	}
}

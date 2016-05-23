package org.vcsreader.lang

import org.junit.Test

import static java.util.concurrent.Executors.newSingleThreadExecutor

class CommandLineTest {
	@Test void "successful command line execution"() {
		def commandLine = new CommandLine("ls").execute()
		assert commandLine.stderr().empty
		assert !commandLine.stdout().empty
		assert commandLine.exceptionStacktrace().empty
		assert commandLine.exitCode() == 0
	}

	@Test void "failed command line execution"() {
		def commandLine = new CommandLine("fake-commandLine").execute()
		assert commandLine.stdout().empty
		assert commandLine.stderr().empty
		assert !commandLine.exceptionStacktrace().empty
		assert commandLine.exitCode() == -123
	}

	@Test(timeout = 1000L) void "kill hanging command"() {
		def commandLine = new CommandLine("sleep", "10000")

		newSingleThreadExecutor().execute {
			Thread.sleep(200)
			commandLine.kill()
		}
		commandLine.execute()

		assert commandLine.exitCode() == 143
	}

	@Test void "command description"() {
		assert new CommandLine("ls", "-l").describe() == "ls -l"
		assert new CommandLine("ls", "-l").workingDir("/").describe() == "ls -l (working directory '/')"
	}
}

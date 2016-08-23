package org.vcsreader.lang

import org.junit.Test

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

import static java.util.concurrent.Executors.newSingleThreadExecutor
import static org.junit.Assert.fail
import static org.vcsreader.lang.CommandLine.exitCodeBeforeFinished

class CommandLineTest {
	@Test void "successful command line execution"() {
		def commandLine = new CommandLine("ls").execute()
		assert commandLine.stderr().empty
		assert !commandLine.stdout().empty
		assert commandLine.exceptionStacktrace().empty
		assert commandLine.exitCode() == 0
	}

	@Test void "failed command line execution"() {
		def commandLine = new CommandLine("fake-commandLine")
		try {
			commandLine.execute()

			fail("Expected exception")
		} catch (CommandLine.Failure e) {
			assert e.cause instanceof IOException
			assert commandLine.stdout().empty
			assert commandLine.stderr().empty
			assert commandLine.exitCode() == exitCodeBeforeFinished
		}
	}

	@Test void "command with failing task executor"() {
		def failingExecutor = { Callable task, String taskName ->
			def result = new FutureResult()
			result.setException(new IllegalStateException())
			result
		} as CommandLine.AsyncExecutor
		def config = CommandLine.Config.defaults.asyncExecutor(failingExecutor)

		def commandLine = new CommandLine(config, "ls")

		try {
			commandLine.execute()
		} catch (CommandLine.Failure e) {
			assert e.cause instanceof ExecutionException
			assert e.cause.cause instanceof IllegalStateException
			assert commandLine.stdout().empty
			assert commandLine.stderr().empty
			assert commandLine.exitCode() == exitCodeBeforeFinished
		}
	}

	@Test(timeout = 1000L)
	void "kill hanging command"() {
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

package org.vcsreader.lang;

import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.vcsreader.lang.StringUtil.shortened;

public class CommandLine {
	public static final int exitCodeBeforeFinished = Integer.MIN_VALUE;

	private final Config config;
	private final String[] commandAndArgs;

	private String stdout = "";
	private String stderr = "";
	private int exitCode = exitCodeBeforeFinished;

	private final AtomicReference<Process> processRef = new AtomicReference<>();
	private final Map<String, String> environment = new HashMap<>();


	public CommandLine(Collection<String> commandAndArgs) {
		this(Config.defaults, commandAndArgs.toArray(new String[0]));
	}

	public CommandLine(String... commandAndArgs) {
		this(Config.defaults, commandAndArgs);
	}

	public CommandLine(Config config, String... commandAndArgs) {
		this.config = config;
		this.commandAndArgs = checkForNulls(commandAndArgs);
	}

	public CommandLine workingDir(String path) {
		if (path == null) return new CommandLine(config.workingDir(null), commandAndArgs);
		else return new CommandLine(config.workingDir(new File(path)), commandAndArgs);
	}

	public CommandLine environment(Map<String, String> map) {
		environment.clear();
		environment.putAll(map);
		return this;
	}

	public CommandLine outputCharset(@NotNull Charset charset) {
		return new CommandLine(config.outputCharset(charset), commandAndArgs);
	}

	public CommandLine charsetAutoDetect(boolean value) {
		return new CommandLine(config.charsetAutoDetect(value), commandAndArgs);
	}

	public CommandLine execute() throws Failure {
		InputStream stdoutInputStream = null;
		InputStream stderrInputStream = null;
		Process process = null;
		try {

			ProcessBuilder builder = new ProcessBuilder(commandAndArgs).directory(config.workingDir);
			builder.environment().putAll(environment);
			process = builder.start();
			processRef.set(process);

			stdoutInputStream = process.getInputStream();
			stderrInputStream = process.getErrorStream();

			Future<String> stdoutFuture = config.asyncExecutor.submit(
					readStreamTask(stdoutInputStream, config.stdoutBufferSize),
					"stdout reader: " + shortened(describe(), 30)
			);
			Future<String> stderrFuture = config.asyncExecutor.submit(
					readStreamTask(stderrInputStream, config.stderrBufferSize),
					"stderr reader: " + shortened(describe(), 30)
			);

			stdout = stdoutFuture.get();
			stderr = stderrFuture.get();

			process.waitFor();
			stdoutInputStream.close();
			stderrInputStream.close();

			process.destroy();
			exitCode = process.exitValue();

		} catch (Exception e) {
			throw new Failure(e);
		} finally {
			// make sure process is stopped in case of exceptions in java code
			if (process != null) process.destroy();
			close(stdoutInputStream);
			close(stderrInputStream);
		}

		return this;
	}

	public void kill() {
		if (processRef.get() != null) {
			processRef.get().destroy();
		}
	}

	@NotNull public String stdout() {
		return stdout;
	}

	@NotNull public String stderr() {
		return stderr;
	}

	public int exitCode() {
		return exitCode;
	}

	public String describe() {
		String result = "";
		for (int i = 0; i < commandAndArgs.length; i++) {
			result += commandAndArgs[i];
			if (i < commandAndArgs.length - 1) result += " ";
		}
		if (config.workingDir != null) {
			result += " (working directory '" + config.workingDir + "')";
		}
		return result;
	}

	@Override public String toString() {
		return describe();
	}

	private Callable<String> readStreamTask(final InputStream stdoutInputStream, final int inputBufferSize) {
		return () -> {
			byte[] bytes = readAsBytes(stdoutInputStream, inputBufferSize);
			return convertToString(bytes);
		};
	}

	private String convertToString(byte[] bytes) throws IOException {
		Charset charset = config.charsetAutoDetect ?
				detectCharset(bytes, config.maxBufferForCharsetDetection) :
				config.outputCharset;
		if (charset == null) charset = config.outputCharset;
		return new String(bytes, charset);
	}

	private static byte[] readAsBytes(InputStream inputStream, int inputBufferSize) throws IOException {
		ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[inputBufferSize];
		int n;
		while ((n = inputStream.read(buffer, 0, buffer.length)) != -1) {
			byteArrayStream.write(buffer, 0, n);
		}
		byteArrayStream.flush();
		return byteArrayStream.toByteArray();
	}

	private static Charset detectCharset(byte[] bytes, int maxBufferForCharsetDetection) {
		UniversalDetector detector = new UniversalDetector(null);
		try {
			detector.handleData(bytes, 0, Math.min(bytes.length, maxBufferForCharsetDetection));
			detector.dataEnd();
		} finally {
			detector.reset();
		}
		String charsetName = detector.getDetectedCharset();
		return charsetName == null ? null : Charset.forName(charsetName);
	}

	private static String[] checkForNulls(String[] command) {
		for (String arg : command) {
			if (arg == null) {
				throw new IllegalStateException("Command cannot have null as inputs, but was: " + Arrays.toString(command));
			}
		}
		return command;
	}

	private static void close(Closeable reader) {
		if (reader == null) return;
		try {
			reader.close();
		} catch (IOException e) {
			throw new Failure(e);
		}
	}

	public static class Failure extends RuntimeException {
		public Failure(Throwable cause) {
			super(cause);
		}
	}

	public interface AsyncExecutor {
		<T> Future<T> submit(Callable<T> task, String taskName);
	}

	public static class Config {
		private static final int defaultBufferSize = 8192;
		private static final File currentDirectory = null;

		public static Config defaults = new Config(
				currentDirectory,
				defaultBufferSize,
				defaultBufferSize,
				Charset.defaultCharset(), false, defaultBufferSize,
				newExecutor());

		private final File workingDir;
		private final int stdoutBufferSize;
		private final int stderrBufferSize;
		private final Charset outputCharset;
		private final boolean charsetAutoDetect;
		private final int maxBufferForCharsetDetection;
		private final AsyncExecutor asyncExecutor;

		public Config(File workingDir, int stdoutBufferSize, int stderrBufferSize, Charset outputCharset,
		              boolean charsetAutoDetect, int maxBufferForCharsetDetection, AsyncExecutor asyncExecutor) {
			this.workingDir = workingDir;
			this.stdoutBufferSize = stdoutBufferSize;
			this.stderrBufferSize = stderrBufferSize;
			this.outputCharset = outputCharset;
			this.charsetAutoDetect = charsetAutoDetect;
			this.maxBufferForCharsetDetection = maxBufferForCharsetDetection;
			this.asyncExecutor = asyncExecutor;
		}

		public Config workingDir(File newWorkingDirectory) {
			return new Config(newWorkingDirectory, stdoutBufferSize, stderrBufferSize, outputCharset, charsetAutoDetect, maxBufferForCharsetDetection, asyncExecutor);
		}

		public Config charsetAutoDetect(boolean value) {
			return new Config(workingDir, stdoutBufferSize, stderrBufferSize, outputCharset, value, maxBufferForCharsetDetection, asyncExecutor);
		}

		public Config outputCharset(Charset charset) {
			return new Config(workingDir, stdoutBufferSize, stderrBufferSize, charset, charsetAutoDetect, maxBufferForCharsetDetection, asyncExecutor);
		}

		public Config asyncExecutor(AsyncExecutor newAsyncExecutor) {
			return new Config(workingDir, stdoutBufferSize, stderrBufferSize, outputCharset, charsetAutoDetect, maxBufferForCharsetDetection, newAsyncExecutor);
		}

		private static AsyncExecutor newExecutor() {
			return new AsyncExecutor() {
				@Override public <T> Future<T> submit(Callable<T> task, String taskName) {
					FutureResult<T> futureResult = new FutureResult<>();
					Runnable runnable = () -> {
						try {
							futureResult.set(task.call());
						} catch (Exception e) {
							futureResult.setException(e);
						}
					};
					Thread thread = new Thread(null, runnable, taskName);
					thread.setDaemon(true);
					thread.start();
					return futureResult;
				}
			};
		}
	}
}

package vcsreader.lang;

import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.lang.StringUtil.shortened;

public class ExternalCommand {
	private static final int exitCodeBeforeFinished = -123;

	private final Config config;
	private final String[] commandAndArgs;

	private String stdout = "";
	private String stderr = "";
	private int exitCode = exitCodeBeforeFinished;
	private Exception exception;

	private final AtomicReference<Process> processRef = new AtomicReference<Process>();


	public ExternalCommand(String... commandAndArgs) {
		this(Config.defaults, commandAndArgs);
	}

	public ExternalCommand(Config config, String... commandAndArgs) {
		this.config = config;
		this.commandAndArgs = checkForNulls(commandAndArgs);
	}

	public ExternalCommand workingDir(String path) {
		if (path == null) return new ExternalCommand(config.workingDir(null), commandAndArgs);
		else return new ExternalCommand(config.workingDir(new File(path)), commandAndArgs);
	}

	public ExternalCommand outputCharset(@NotNull Charset charset) {
		return new ExternalCommand(config.outputCharset(charset), commandAndArgs);
	}

	public ExternalCommand charsetAutoDetect(boolean value) {
		return new ExternalCommand(config.charsetAutoDetect(value), commandAndArgs);
	}

	public ExternalCommand execute() {
		InputStream stdoutInputStream = null;
		InputStream stderrInputStream = null;
		try {

			Process process = new ProcessBuilder(commandAndArgs).directory(config.workingDirectory).start();
			processRef.set(process);

			stdoutInputStream = process.getInputStream();
			stderrInputStream = process.getErrorStream();

			Future<String> stdoutFuture = config.taskExecutor.submit(
					readStreamTask(stdoutInputStream, config.stdoutBufferSize),
					"stdout reader: " + shortened(describe(), 30)
			);
			Future<String> stderrFuture = config.taskExecutor.submit(
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
			exception = e;
		} finally {
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

	public boolean hasNoExceptions() {
		return exception == null;
	}

	public String exceptionStacktrace() {
		if (exception == null) return "";

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		try {
			printWriter.append('\n');
			exception.printStackTrace(printWriter);
		} finally {
			printWriter.close();
		}
		return stringWriter.toString();
	}

	public String describe() {
		String result = "";
		for (int i = 0; i < commandAndArgs.length; i++) {
			result += commandAndArgs[i];
			if (i < commandAndArgs.length - 1) result += " ";
		}
		if (config.workingDirectory != null) {
			result += " (working directory '" + config.workingDirectory + "')";
		}
		return result;
	}

	private Callable<String> readStreamTask(final InputStream stdoutInputStream, final int inputBufferSize) {
		return new Callable<String>() {
			@Override public String call() throws Exception {
				byte[] bytes = readAsBytes(stdoutInputStream, inputBufferSize);
				return convertToString(bytes);
			}
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
		} catch (IOException ignored) {
		}
	}

	public interface TaskExecutor {
		<T> Future<T> submit(Callable<T> task, String taskName);
	}

	public static class Config {
		private static final int defaultBufferSize = 8192;
		private static final File currentDirectory = null;

		public static Config defaults = new Config(
				currentDirectory,
				defaultBufferSize,
				defaultBufferSize,
				defaultBufferSize,
				false,
				Charset.defaultCharset(),
				newThreadExecutor());


		private final File workingDirectory;
		private final int stdoutBufferSize;
		private final int stderrBufferSize;
		private final int maxBufferForCharsetDetection;
		private final boolean charsetAutoDetect;
		private final Charset outputCharset;
		private final TaskExecutor taskExecutor;

		public Config(File workingDirectory, int stdoutBufferSize, int stderrBufferSize, int maxBufferForCharsetDetection,
		              boolean charsetAutoDetect, Charset outputCharset, TaskExecutor taskExecutor) {
			this.workingDirectory = workingDirectory;
			this.stdoutBufferSize = stdoutBufferSize;
			this.stderrBufferSize = stderrBufferSize;
			this.maxBufferForCharsetDetection = maxBufferForCharsetDetection;
			this.charsetAutoDetect = charsetAutoDetect;
			this.outputCharset = outputCharset;
			this.taskExecutor = taskExecutor;
		}

		public Config charsetAutoDetect(boolean value) {
			return new Config(workingDirectory, stdoutBufferSize, stderrBufferSize, maxBufferForCharsetDetection, value, outputCharset, taskExecutor);
		}

		public Config outputCharset(Charset charset) {
			return new Config(workingDirectory, stdoutBufferSize, stderrBufferSize, maxBufferForCharsetDetection, charsetAutoDetect, charset, taskExecutor);
		}

		public Config workingDir(File file) {
			return new Config(file, stdoutBufferSize, stderrBufferSize, maxBufferForCharsetDetection, charsetAutoDetect, outputCharset, taskExecutor);
		}

		private static TaskExecutor newThreadExecutor() {
			return new TaskExecutor() {
				@Override public <T> Future<T> submit(final Callable<T> task, String taskName) {
					final FutureResult<T> futureResult = new FutureResult<T>();
					Runnable runnable = new Runnable() {
						@Override public void run() {
							try {
								futureResult.set(task.call());
							} catch (Exception e) {
								futureResult.setException(e);
							}
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

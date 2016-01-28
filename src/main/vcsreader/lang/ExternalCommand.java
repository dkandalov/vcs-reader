package vcsreader.lang;

import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ExternalCommand {
	private static final int exitCodeBeforeFinished = -123;

	private final Config config;
	private final String[] commandAndArgs;

	private String stdout;
	private String stderr;
	private byte[] stdoutBytes;
	private byte[] stderrBytes;
	private int exitCode = exitCodeBeforeFinished;
	private Exception exception;

	private final AtomicReference<Process> processRef = new AtomicReference<Process>();


	public ExternalCommand(String... commandAndArgs) {
		this(Config.defaults, commandAndArgs);
	}

	public ExternalCommand(Config config, String... commandAndArgs) {
		this.config = config;
		this.commandAndArgs = checkForNulls(commandAndArgs);
		this.stdoutBytes = new byte[0];
		this.stderrBytes = new byte[0];
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
			// TODO read from both in one loop to avoid process being stuck if one of outputs is full?
			stdoutBytes = readAsBytes(stdoutInputStream, config.stdoutBufferSize);
			stderrBytes = readAsBytes(stderrInputStream, config.stderrBufferSize);

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
		if (stdout == null) {
			Charset charset = config.charsetAutoDetect ?
					detectCharset(stdoutBytes, config.maxBufferForCharsetDetection) :
					config.outputCharset;
			if (charset == null) charset = config.outputCharset;
			stdout = new String(stdoutBytes, charset);
		}
		return stdout;
	}

	@NotNull public String stderr() {
		if (stderr == null) {
			Charset charset = config.charsetAutoDetect ?
					detectCharset(stderrBytes, config.maxBufferForCharsetDetection) :
					config.outputCharset;
			if (charset == null) charset = config.outputCharset;
			stderr = new String(stderrBytes, charset);
		}
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


	public static class Config {
		private static final int defaultBufferSize = 8192;
		private static final File currentDirectory = null;

		public static Config defaults = new Config(
				currentDirectory,
				defaultBufferSize,
				defaultBufferSize,
				defaultBufferSize,
				false,
				Charset.defaultCharset()
		);

		private final File workingDirectory;
		private final int stdoutBufferSize;
		private final int stderrBufferSize;
		private final int maxBufferForCharsetDetection;
		private final boolean charsetAutoDetect;
		private final Charset outputCharset;

		public Config(File workingDirectory, int stdoutBufferSize, int stderrBufferSize, int maxBufferForCharsetDetection,
		              boolean charsetAutoDetect, Charset outputCharset) {
			this.workingDirectory = workingDirectory;
			this.stdoutBufferSize = stdoutBufferSize;
			this.stderrBufferSize = stderrBufferSize;
			this.maxBufferForCharsetDetection = maxBufferForCharsetDetection;
			this.charsetAutoDetect = charsetAutoDetect;
			this.outputCharset = outputCharset;
		}

		public Config charsetAutoDetect(boolean value) {
			return new Config(workingDirectory, stdoutBufferSize, stderrBufferSize, maxBufferForCharsetDetection, value, outputCharset);
		}

		public Config outputCharset(Charset charset) {
			return new Config(workingDirectory, stdoutBufferSize, stderrBufferSize, maxBufferForCharsetDetection, charsetAutoDetect, charset);
		}

		public Config workingDir(File file) {
			return new Config(file, stdoutBufferSize, stderrBufferSize, maxBufferForCharsetDetection, charsetAutoDetect, outputCharset);

		}
	}
}

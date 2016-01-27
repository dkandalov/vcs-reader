package vcsreader.lang;

import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ExternalCommand {
	private static final File currentDirectory = null;
	private static final int exitCodeBeforeFinished = -123;
	private static final int defaultBufferSize = 8192;
	private static final int maxBytesToUseForCharsetDetection = 8192;

	private final String[] commandAndArgs;
	private String stdout;
	private String stderr;
	private byte[] stdoutBytes;
	private byte[] stderrBytes;
	private int exitCode = exitCodeBeforeFinished;
	private Exception exception;

	private int inputBufferSize = defaultBufferSize;
	private File workingDirectory = currentDirectory;
	private Charset outputCharset = Charset.defaultCharset();
	private boolean charsetAutoDetect = false;

	private final AtomicReference<Process> processRef = new AtomicReference<Process>();


	public ExternalCommand(String... commandAndArgs) {
		this(defaultBufferSize, commandAndArgs);
	}

	public ExternalCommand(int inputBufferSize, String... commandAndArgs) {
		this.inputBufferSize = inputBufferSize;
		this.commandAndArgs = checkForNulls(commandAndArgs);
		this.stdoutBytes = new byte[0];
		this.stderrBytes = new byte[0];
	}

	public ExternalCommand workingDir(String path) {
		workingDirectory = new File(path);
		return this;
	}

	public ExternalCommand outputCharset(@NotNull Charset charset) {
		outputCharset = charset;
		return this;
	}

	public ExternalCommand charsetAutoDetect(boolean value) {
		charsetAutoDetect = value;
		return this;
	}

	public ExternalCommand execute() {
		InputStream stdoutInputStream = null;
		InputStream stderrInputStream = null;
		try {

			Process process = new ProcessBuilder(commandAndArgs).directory(workingDirectory).start();
			processRef.set(process);

			stdoutInputStream = process.getInputStream();
			stderrInputStream = process.getErrorStream();
			// TODO read from both in one loop to avoid process being stuck if one of outputs is full?
			stdoutBytes = readAsBytes(stdoutInputStream, inputBufferSize);
			stderrBytes = readAsBytes(stderrInputStream, inputBufferSize);

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
			Charset charset = charsetAutoDetect ? detectCharset(stdoutBytes) : outputCharset;
			if (charset == null) charset = outputCharset;
			stdout = new String(stdoutBytes, charset);
		}
		return stdout;
	}

	@NotNull public String stderr() {
		if (stderr == null) {
			Charset charset = charsetAutoDetect ? detectCharset(stderrBytes) : outputCharset;
			if (charset == null) charset = outputCharset;
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
		if (workingDirectory != null) {
			result += " (working directory '" + workingDirectory + "')";
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

	private static Charset detectCharset(byte[] bytes) {
		UniversalDetector detector = new UniversalDetector(null);
		try {
			detector.handleData(bytes, 0, Math.min(bytes.length, maxBytesToUseForCharsetDetection));
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
}

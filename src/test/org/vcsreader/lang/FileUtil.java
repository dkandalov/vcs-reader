package org.vcsreader.lang;

import java.io.File;

public class FileUtil {
	public static File findSequentNonExistentFile(File parentFolder, String filePrefix, String postfix) {
		int i = 0;
		File candidate = new File(parentFolder, filePrefix + Integer.toString(i) + postfix);
		while (candidate.exists()) {
			i++;
			candidate = new File(parentFolder, filePrefix + Integer.toString(i) + postfix);
		}
		return candidate;
	}

	public static File tempDirectoryFile() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	public static File deleteOnShutdown(File file) {
		boolean enabled = Boolean.parseBoolean(System.getProperty("vcsreader.test.deleteFilesOnShutdown", "true"));
		if (!enabled) return file;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (!file.exists()) return;
				boolean wasDeleted = doDelete(file);
				if (!wasDeleted) {
					System.out.println("Failed to delete file on shutdown: " + file.getAbsolutePath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		return file;
	}

	private static boolean doDelete(File file) {
		if (!file.isDirectory()) return file.delete();
		File[] files = file.listFiles();
		if (files == null) return file.delete();

		for (File child : files) {
			if (!doDelete(child)) return false;
		}
		return file.delete();
	}
}

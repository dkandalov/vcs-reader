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
}

package com.eeeffff.limiter.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.eeeffff.limiter.common.exception.IpLimitException;

public class FileUtils {
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String POIFILES = "poifiles";
	private static final String CACHE = "excache";
	private static final int WRITE_BUFF_SIZE = 8192;

	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = openInputStream(file);
		try {
			long fileLength = file.length();
			return (fileLength > 0L) ? IoUtils.toByteArray(in, (int) fileLength) : IoUtils.toByteArray(in);
		} finally {
			in.close();
		}
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canRead()) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	public static void writeToFile(File file, InputStream inputStream) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);

			byte[] buffer = new byte[WRITE_BUFF_SIZE];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer, 0, WRITE_BUFF_SIZE)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			throw new IpLimitException("Can not create temporary file!", e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					throw new IpLimitException("Can not close 'outputStream'!", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new IpLimitException("Can not close 'inputStream'", e);
				}
			}
		}
	}

	public static void createPoiFilesDirectory() {
		createTmpDirectory(POIFILES);
	}

	public static File createCacheTmpFile() {
		File directory = createTmpDirectory(CACHE);
		File cache = new File(directory.getPath(), UUID.randomUUID().toString());
		if (!cache.mkdir()) {
			throw new IpLimitException("Can not create temp file!");
		}
		return cache;
	}

	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	public static File createTmpDirectory(String path) {
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		if (tmpDir == null) {
			throw new RuntimeException(
					"Systems temporary directory not defined - set the -Djava.io.tmpdir jvm property!");
		}

		File directory = new File(tmpDir, path);
		if (!directory.exists()) {
			syncCreatePoiFilesDirectory(directory);
		}
		return directory;
	}

	private static synchronized void syncCreatePoiFilesDirectory(File directory) {
		if (!directory.exists())
			directory.mkdirs();
	}
}

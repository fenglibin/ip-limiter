package com.eeefff.limiter.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
	public static final int EOF = -1;
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			copy(input, output);
			return output.toByteArray();
		} finally {
			output.toByteArray();
		}
	}

	public static byte[] toByteArray(InputStream input, int size) throws IOException {
		if (size < 0) {
			throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
		}
		if (size == 0) {
			return new byte[0];
		}
		byte[] data = new byte[size];
		int offset = 0;
		int read;
		while (offset < size && (read = input.read(data, offset, size - offset)) != -1) {
			offset += read;
		}
		if (offset != size) {
			throw new IOException("Unexpected read size. current: " + offset + ", expected: " + size);
		}
		return data;
	}

	public static int copy(InputStream input, OutputStream output) throws IOException {
		long count = 0L;

		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		if (count > 2147483647L) {
			return -1;
		}
		return (int) count;
	}
}

package hasoffer.base.model;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MemoryReadOnlyFile {
	private String fileName;
	private byte[] data;

	public MemoryReadOnlyFile(File file) throws IOException {
		this.fileName = file.getName();
		this.data = FileUtils.readFileToByteArray(file);
	}

	public MemoryReadOnlyFile(String fileName, byte[] data) {
		super();
		this.fileName = fileName;
		this.data = data;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getData() {
		return data;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.data);
	}

	@Override
	public String toString() {
		return "MemoryFile [fileName=" + fileName + ", data="
				+ Arrays.toString(data) + "]";
	}
}

package net.noha.tools.ant.yuicompressor.tasks;

public class FileType {
	public static final String JS_FILE = ".js";
	public static final String CSS_FILE = ".css";

	private final String suffix;

	public FileType(String suffix) {
		this.suffix = suffix;
	}

	public String getSuffix() {
		return this.suffix;
	}

	public static String getFileType(String fileName) {
		if (fileName == null || "".equals(fileName)) {
			return null;
		}

		return (fileName.endsWith(JS_FILE)) ? JS_FILE : (fileName
				.endsWith(CSS_FILE)) ? CSS_FILE : null;
	}
}

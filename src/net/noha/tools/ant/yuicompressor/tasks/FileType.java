package net.noha.tools.ant.yuicompressor.tasks;

public enum FileType {
	JS_FILE(".js"), CSS_FILE(".css");

	private final String suffix;

	FileType(String suffix) {
		this.suffix = suffix;
	}

	public String getSuffix() {
		return this.suffix;
	}

	public static FileType getFileType(String fileName) {
		if (fileName == null || "".equals(fileName)) {
			return null;
		}

		return (fileName.endsWith(JS_FILE.getSuffix())) ? JS_FILE : (fileName
				.endsWith(CSS_FILE.getSuffix())) ? CSS_FILE : null;
	}
}

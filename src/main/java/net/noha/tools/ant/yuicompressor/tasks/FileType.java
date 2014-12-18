package net.noha.tools.ant.yuicompressor.tasks;

public class FileType {
    public static final String JS_FILE = ".js";
    public static final String CSS_FILE = ".css";
    public static final String XML_FILE = ".xml";
    public static final String HTML_FILE = ".html";
    public static final String XHTML_FILE = ".xhtml";

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

        return (fileName.endsWith(JS_FILE)) ? JS_FILE : (fileName.endsWith(CSS_FILE)) ? CSS_FILE : (fileName
                .endsWith(XML_FILE)) ? XML_FILE : (fileName.endsWith(HTML_FILE)) ? HTML_FILE : (fileName
                .endsWith(XHTML_FILE)) ? XHTML_FILE : null;
    }
}

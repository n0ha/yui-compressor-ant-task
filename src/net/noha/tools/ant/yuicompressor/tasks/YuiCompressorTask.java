/**
 * Copyright (c) 2007, Yahoo! Inc. All rights reserved.
 * Copyright (c) 2007, Viktor Lieskovsky
 *
 * The YUI Compressor was written and is maintained by:
 *     	Julien Lecomte <jlecomte@yahoo-inc.com>
 * The Ant task was written by:
 *      Viktor Lieskovsky <viktor.lieskovsky@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the <ORGANIZATION> nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.noha.tools.ant.yuicompressor.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.google.common.base.Charsets;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.XmlCompressor;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * @author Viktor Lieskovsky
 */
public class YuiCompressorTask extends MatchingTask {

    protected File fromDir;

    protected File toDir;

    // properties with default values
    protected Charset charset = Charsets.UTF_8;
    protected int lineBreakPosition = -1;
    protected boolean munge = false;
    protected boolean warn = true;
    private boolean enabled = true;
    protected boolean preserveAllSemiColons = true;
    protected boolean optimize = true;
    protected boolean verbose = true;

    // suffixes
    protected String jsSuffix = ".js";
    protected String cssSuffix = ".css";
    protected String xmlSuffix = ".xml";
    protected String htmlSuffix = ".html";
    protected String xhtmlSuffix = ".xhtml";

    // stats
    private final CompressionStatistics stats = new CompressionStatistics();

    private void compressFile(final File inFile, final File outFile, final String fileType) throws EvaluatorException,
            BuildException {
        // do not recompress when outFile is newer
        // always recompress when outFile and inFile are exactly the same file
        if (outFile.isFile() && !inFile.getAbsolutePath().equals(outFile.getAbsolutePath())) {
            if (outFile.lastModified() >= inFile.lastModified()) {
                return;
            }
        }

        try {
            // prepare input file
            Reader in = openFile(inFile);

            // prepare output file
            outFile.getParentFile().mkdirs();
            Writer out = new OutputStreamWriter(new FileOutputStream(outFile), charset);

            if (fileType.equals(FileType.JS_FILE)) {
                final JavaScriptCompressor compressor = createJavaScriptCompressor(in);
                compressor.compress(out, lineBreakPosition, munge, warn, preserveAllSemiColons, !optimize);
            } else if (fileType.equals(FileType.CSS_FILE)) {
                final CssCompressor compressor = new CssCompressor(in);
                compressor.compress(out, lineBreakPosition);
            } else if (fileType.equals(FileType.HTML_FILE) || fileType.equals(FileType.XHTML_FILE)) {
                final HtmlCompressor compressor = new HtmlCompressor();
                out.write(compressor.compress(readerToString(in)));
            } else if (fileType.equals(FileType.XML_FILE)){
                final XmlCompressor compressor = new XmlCompressor();
                out.write(compressor.compress(readerToString(in)));
            }

            // close all streams
            in.close();
            in = null;
            out.close();
            out = null;

            if (verbose) {
                log(stats.getFileStats(inFile, outFile, fileType));
            }
        } catch (final IOException ioe) {
            throw new BuildException("I/O Error when compressing file", ioe);
        }
    }

    private String readerToString(Reader in) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        // Read the stream...
        int c;
        while ((c = in.read()) != -1) {
            stringBuilder.append((char) c);
        }
        return stringBuilder.toString();
    }

    private void copyFile(final File srcFile, final File targetFile) throws IOException {
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile(); // if necessary, creates the target file

        final FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
        final FileChannel dstChannel = new FileOutputStream(targetFile).getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

        srcChannel.close();
        dstChannel.close();
    }

    private JavaScriptCompressor createJavaScriptCompressor(final Reader in) throws IOException {
        final JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

            public void error(final String message, final String sourceName, final int line, final String lineSource,
                    final int lineOffset) {
                log(getMessage(sourceName, message, line, lineOffset), Project.MSG_ERR);
            }

            private String getMessage(final String source, final String message, final int line, final int lineOffset) {
                String logMessage;
                if (line < 0) {
                    logMessage = source != null ? new StringBuilder().append(source).append(":").toString()
                            : new StringBuilder().append("").append(message).toString();
                } else {
                    logMessage = source != null ? new StringBuilder().append(source).append(":").toString()
                            : new StringBuilder().append("").append(line).append(":").append(lineOffset).append(":")
                                    .append(message).toString();
                }
                return logMessage;
            }

            public EvaluatorException runtimeError(final String message, final String sourceName, final int line,
                    final String lineSource, final int lineOffset) {
                log(getMessage(sourceName, message, line, lineOffset), Project.MSG_ERR);
                return new EvaluatorException(message);
            }

            public void warning(final String message, final String sourceName, final int line, final String lineSource,
                    final int lineOffset) {
                log(getMessage(sourceName, message, line, lineOffset), Project.MSG_WARN);
            }
        });
        return compressor;
    }

    @Override
    public void execute() {
        validateDirs();

        final DirectoryScanner ds = getDirectoryScanner(fromDir);
        final String[] files = ds.getIncludedFiles();

        for (final String file : files) {
            final File inFile = new File(fromDir.getAbsolutePath(), file);
            final String fileType = FileType.getFileType(file);
            if (fileType == null) {
                continue;
            }

            final File outFile = new File(toDir.getAbsolutePath(), file.replaceFirst(fileType + "$",
                    newFileSuffix(fileType)));
            if (isEnabled()) {
                compressFile(inFile, outFile, fileType);
            } else {
                try {
                    copyFile(inFile, outFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (verbose) {
            log(stats.getXmlStats());
            log(stats.getHtmlStats());
            log(stats.getXhtmlStats());
            log(stats.getJsStats());
            log(stats.getCssStats());
            log(stats.getTotalStats());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    private String newFileSuffix(final String fileType) {
        if (fileType.equals(FileType.JS_FILE)) {
            return jsSuffix;
        } else if (fileType.equals(FileType.CSS_FILE)) {
            return cssSuffix;
        } else if (fileType.equals(FileType.XML_FILE)) {
            return xmlSuffix;
        } else if (fileType.equals(FileType.HTML_FILE)) {
            return htmlSuffix;
        } else if (fileType.equals(FileType.XHTML_FILE)) {
            return xhtmlSuffix;
        }
        return null;
    }

    private Reader openFile(final File file) throws BuildException {
        Reader in;
        try {
            in = new InputStreamReader(new FileInputStream(file), charset);
        } catch (final UnsupportedCharsetException uche) {
            throw new BuildException("Unsupported charset name: " + charset, uche);
        } catch (final IOException ioe) {
            throw new BuildException("I/O Error when reading input file", ioe);
        }
        return in;
    }

    public void setCharset(final String charset) {
        if (charset.equalsIgnoreCase("ISO_8859_1")) {
            this.charset = Charsets.ISO_8859_1;
        } else if (charset.equalsIgnoreCase("US_ASCII")) {
            this.charset = Charsets.US_ASCII;
        } else if (charset.equalsIgnoreCase("UTF_16")) {
            this.charset = Charsets.UTF_16;
        } else if (charset.equalsIgnoreCase("UTF_16BE")) {
            this.charset = Charsets.UTF_16BE;
        } else if (charset.equalsIgnoreCase("UTF_16LE")) {
            this.charset = Charsets.UTF_16LE;
        } else {
            this.charset = Charsets.UTF_8;
        }
    }

    public void setCssSuffix(final String cssSuffix) {
        this.cssSuffix = cssSuffix;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setFromDir(final File fromDir) {
        this.fromDir = fromDir;
    }

    public void setJsSuffix(final String jsSuffix) {
        this.jsSuffix = jsSuffix;
    }

    public void setLineBreakPosition(final int lineBreakPosition) {
        this.lineBreakPosition = lineBreakPosition;
    }

    public void setMunge(final boolean munge) {
        this.munge = munge;
    }

    public void setOptimize(final boolean optimize) {
        this.optimize = optimize;
    }

    public void setPreserveAllSemiColons(final boolean preserveAllSemiColons) {
        this.preserveAllSemiColons = preserveAllSemiColons;
    }

    public void setToDir(final File toDir) {
        this.toDir = toDir;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public void setWarn(final boolean warn) {
        this.warn = warn;
    }

    private void validateDirs() throws BuildException {
        if (!fromDir.isDirectory()) {
            throw new BuildException(fromDir + " is not a valid directory");
        }
        if (!toDir.isDirectory()) {
            throw new BuildException(toDir + " is not a valid directory");
        }
    }
}

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
import java.nio.charset.UnsupportedCharsetException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * @author Viktor Lieskovsky
 */
public class YuiCompressorTask extends MatchingTask {

	protected File fromDir;
	protected File toDir;

	// properties with default values
	protected String charset = "ISO-8859-1";
	protected int lineBreakPosition = -1;
	protected boolean munge = false;
	protected boolean warn = true;
	private boolean enabled = true;
	protected boolean preserveAllSemiColons = true;
	protected boolean optimize = true;
	protected boolean verbose = true;

	// suffixes
	protected String jsSuffix = "-min.js";
	protected String cssSuffix = "-min.css";

	// stats
	private CompressionStatistics stats = new CompressionStatistics();

	public void execute() {
		validateDirs();

		DirectoryScanner ds = getDirectoryScanner(fromDir);
		String[] files = ds.getIncludedFiles();

		for (int i = 0; i < files.length; i++) {
			File inFile = new File(fromDir.getAbsolutePath(), files[i]);
			String fileType = FileType.getFileType(files[i]);
			if (fileType == null) {
				continue;
			}

			String newSuffix = (fileType.equals(FileType.JS_FILE)) ? jsSuffix : cssSuffix;
			File outFile = new File(toDir.getAbsolutePath(), files[i].replaceFirst(fileType + "$", newSuffix));
			if (isEnabled())
				compressFile(inFile, outFile, fileType);
			else {
				try {
					copyFile(inFile, outFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (verbose) {
			log(stats.getJsStats());
			log(stats.getCssStats());
			log(stats.getTotalStats());
		}
	}

	public static void copyFile(File srcFile, File targetFile) throws IOException {
		targetFile.getParentFile().mkdirs();
		targetFile.createNewFile(); // if necessary, creates the target file

		FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
		FileChannel dstChannel = new FileOutputStream(targetFile).getChannel();
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		srcChannel.close();
		dstChannel.close();
	}

	private void compressFile(File inFile, File outFile, String fileType) throws EvaluatorException, BuildException {
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
				JavaScriptCompressor compressor = createJavaScriptCompressor(in);
				compressor.compress(out, lineBreakPosition, munge, warn, preserveAllSemiColons, !optimize);
			} else if (fileType.equals(FileType.CSS_FILE)) {
				CssCompressor compressor = new CssCompressor(in);
				compressor.compress(out, lineBreakPosition);
			}

			// close all streams
			in.close();
			in = null;
			out.close();
			out = null;

			if (verbose) {
				log(stats.getFileStats(inFile, outFile, fileType));
			}
		} catch (IOException ioe) {
			throw new BuildException("I/O Error when compressing file", ioe);
		}
	}

	private JavaScriptCompressor createJavaScriptCompressor(Reader in) throws IOException {
		JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

			private String getMessage(String source, String message, int line, int lineOffset) {
				String logMessage;
				if (line < 0) {
					logMessage = (source != null) ? source + ":" : "" + message;
				} else {
					logMessage = (source != null) ? source + ":" : "" + line + ":" + lineOffset + ":" + message;
				}
				return logMessage;
			}

			public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
				log(getMessage(sourceName, message, line, lineOffset), Project.MSG_WARN);
			}

			public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
				log(getMessage(sourceName, message, line, lineOffset), Project.MSG_ERR);

			}

			public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
				log(getMessage(sourceName, message, line, lineOffset), Project.MSG_ERR);
				return new EvaluatorException(message);
			}
		});
		return compressor;
	}

	private Reader openFile(File file) throws BuildException {
		Reader in;
		try {
			in = new InputStreamReader(new FileInputStream(file), charset);
		} catch (UnsupportedCharsetException uche) {
			throw new BuildException("Unsupported charset name: " + charset, uche);
		} catch (IOException ioe) {
			throw new BuildException("I/O Error when reading input file", ioe);
		}
		return in;
	}

	private void validateDirs() throws BuildException {
		if (!fromDir.isDirectory())
			throw new BuildException(fromDir + " is not a valid directory");
		if (!toDir.isDirectory())
			throw new BuildException(toDir + " is not a valid directory");
	}

	public void setToDir(File toDir) {
		this.toDir = toDir;
	}

	public void setFromDir(File fromDir) {
		this.fromDir = fromDir;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setLineBreakPosition(int lineBreakPosition) {
		this.lineBreakPosition = lineBreakPosition;
	}

	public void setMunge(boolean munge) {
		this.munge = munge;
	}

	public void setWarn(boolean warn) {
		this.warn = warn;
	}

	public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
		this.preserveAllSemiColons = preserveAllSemiColons;
	}

	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	public void setJsSuffix(String jsSuffix) {
		this.jsSuffix = jsSuffix;
	}

	public void setCssSuffix(String cssSuffix) {
		this.cssSuffix = cssSuffix;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}

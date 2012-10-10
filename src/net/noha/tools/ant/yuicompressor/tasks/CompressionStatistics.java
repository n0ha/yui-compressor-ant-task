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

/**
 * @author Viktor Lieskovsky
 */
public class CompressionStatistics {

    private long fromBytes = 0L;
    private long toBytes = 0L;
    private long fileCount = 0L;

    private long xmlFromBytes = 0L;
    private long xmlToBytes = 0L;
    private long xmlFileCount = 0L;

    private long htmlFromBytes = 0L;
    private long htmlToBytes = 0L;
    private long htmlFileCount = 0L;

    private long xhtmlFromBytes = 0L;
    private long xhtmlToBytes = 0L;
    private long xhtmlFileCount = 0L;

    private long jsFromBytes = 0L;
    private long jsToBytes = 0L;
    private long jsFileCount = 0L;

    private long cssFromBytes = 0L;
    private long cssToBytes = 0L;
    private long cssFileCount = 0L;

    public String getCssStats() {
        if (cssFileCount == 0) {
            return "[CSS] No files to compress, or all files already up to date";
        }

        final int percentage = (int) Math.floor((double) cssToBytes / (double) cssFromBytes * 100);
        final long fromKBytes = (long) Math.floor((double) cssFromBytes / (double) 1024);
        final long toKBytes = (long) Math.floor((double) cssToBytes / (double) 1024);
        final long saved = fromKBytes - toKBytes;

        return "[CSS] Compressed " + cssFileCount + " files to " + percentage + "% (" + fromKBytes + "KB to "
                + toKBytes + "KB, saving " + saved + "KB)";
    }

    public String getFileStats(final File inFile, final File outFile, final String fileType) {
        // update cummulated statistics
        fromBytes += inFile.length();
        toBytes += outFile.length();
        fileCount++;

        if (fileType.equals(FileType.XML_FILE)) {
            xmlFromBytes += inFile.length();
            xmlToBytes += outFile.length();
            xmlFileCount++;
        } else if (fileType.equals(FileType.HTML_FILE)) {
            htmlFromBytes += inFile.length();
            htmlToBytes += outFile.length();
            htmlFileCount++;
        } else if (fileType.equals(FileType.XHTML_FILE)) {
            xhtmlFromBytes += inFile.length();
            xhtmlToBytes += outFile.length();
            xhtmlFileCount++;
        } else if (fileType.equals(FileType.JS_FILE)) {
            jsFromBytes += inFile.length();
            jsToBytes += outFile.length();
            jsFileCount++;
        } else {
            cssFromBytes += inFile.length();
            cssToBytes += outFile.length();
            cssFileCount++;
        }

        final int percentage = (int) Math.floor((double) outFile.length() / (double) inFile.length() * 100);
        return "[" + percentage + "%] " + inFile.getName() + " [" + inFile.length() + "] ---> " + outFile.getName()
                + " [" + outFile.length() + "]";
    }

    public String getHtmlStats() {
        if (htmlFileCount == 0) {
            return "[HTML] No files to compress, or all files already up to date";
        }

        final int percentage = (int) Math.floor((double) htmlToBytes / (double) htmlFromBytes * 100);
        final long fromKBytes = (long) Math.floor((double) htmlFromBytes / (double) 1024);
        final long toKBytes = (long) Math.floor((double) htmlToBytes / (double) 1024);
        final long saved = fromKBytes - toKBytes;

        return "[HTML] Compressed " + htmlFileCount + " files to " + percentage + "% (" + fromKBytes + "KB to "
                + toKBytes + "KB, saving " + saved + "KB)";
    }

    public String getJsStats() {
        if (jsFileCount == 0) {
            return "[JavaScript] No files to compress, or all files already up to date";
        }

        final int percentage = (int) Math.floor((double) jsToBytes / (double) jsFromBytes * 100);
        final long fromKBytes = (long) Math.floor((double) jsFromBytes / (double) 1024);
        final long toKBytes = (long) Math.floor((double) jsToBytes / (double) 1024);
        final long saved = fromKBytes - toKBytes;

        return "[JavaScript] Compressed " + jsFileCount + " files to " + percentage + "% (" + fromKBytes + "KB to "
                + toKBytes + "KB, saving " + saved + "KB)";
    }

    public String getTotalStats() {
        if (fileCount == 0) {
            return "No files to compress, or all files already up to date";
        }

        final int percentage = (int) Math.floor((double) toBytes / (double) fromBytes * 100);
        final long fromKBytes = (long) Math.floor((double) fromBytes / (double) 1024);
        final long toKBytes = (long) Math.floor((double) toBytes / (double) 1024);
        final long saved = fromKBytes - toKBytes;

        return "Compressed " + fileCount + " files to " + percentage + "% (" + fromKBytes + "KB to " + toKBytes
                + "KB, saving " + saved + "KB)";
    }

    public String getXhtmlStats() {
        if (xhtmlFileCount == 0) {
            return "[XHTML] No files to compress, or all files already up to date";
        }

        final int percentage = (int) Math.floor((double) xhtmlToBytes / (double) xhtmlFromBytes * 100);
        final long fromKBytes = (long) Math.floor((double) xhtmlFromBytes / (double) 1024);
        final long toKBytes = (long) Math.floor((double) xhtmlToBytes / (double) 1024);
        final long saved = fromKBytes - toKBytes;

        return "[XHTML] Compressed " + xhtmlFileCount + " files to " + percentage + "% (" + fromKBytes + "KB to "
                + toKBytes + "KB, saving " + saved + "KB)";
    }

    public String getXmlStats() {
        if (xmlFileCount == 0) {
            return "[XML] No files to compress, or all files already up to date";
        }

        final int percentage = (int) Math.floor((double) xmlToBytes / (double) xmlFromBytes * 100);
        final long fromKBytes = (long) Math.floor((double) xmlFromBytes / (double) 1024);
        final long toKBytes = (long) Math.floor((double) xmlToBytes / (double) 1024);
        final long saved = fromKBytes - toKBytes;

        return "[XML] Compressed " + xmlFileCount + " files to " + percentage + "% (" + fromKBytes + "KB to "
                + toKBytes + "KB, saving " + saved + "KB)";
    }

}

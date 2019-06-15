package com.oneplus.lib.preference;

import com.android.settingslib.accessibility.AccessibilityUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.xmlpull.v1.XmlSerializer;

public class FastXmlSerializer implements XmlSerializer {
    private static final int BUFFER_LEN = 8192;
    private static final String[] ESCAPE_TABLE = new String[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "&quot;", null, null, null, "&amp;", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "&lt;", null, "&gt;", null};
    private static String sSpace = "                                                              ";
    private ByteBuffer mBytes = ByteBuffer.allocate(8192);
    private CharsetEncoder mCharset;
    private boolean mInTag;
    private boolean mIndent = false;
    private boolean mLineStart = true;
    private int mNesting = 0;
    private OutputStream mOutputStream;
    private int mPos;
    private final char[] mText = new char[8192];
    private Writer mWriter;

    private void append(char c) throws IOException {
        int pos = this.mPos;
        if (pos >= 8191) {
            flush();
            pos = this.mPos;
        }
        this.mText[pos] = c;
        this.mPos = pos + 1;
    }

    private void append(String str, int i, int length) throws IOException {
        int end;
        if (length > 8192) {
            end = i + length;
            while (i < end) {
                int next = i + 8192;
                append(str, i, next < end ? 8192 : end - i);
                i = next;
            }
            return;
        }
        end = this.mPos;
        if (end + length > 8192) {
            flush();
            end = this.mPos;
        }
        str.getChars(i, i + length, this.mText, end);
        this.mPos = end + length;
    }

    private void append(char[] buf, int i, int length) throws IOException {
        int end;
        if (length > 8192) {
            end = i + length;
            while (i < end) {
                int next = i + 8192;
                append(buf, i, next < end ? 8192 : end - i);
                i = next;
            }
            return;
        }
        end = this.mPos;
        if (end + length > 8192) {
            flush();
            end = this.mPos;
        }
        System.arraycopy(buf, i, this.mText, end, length);
        this.mPos = end + length;
    }

    private void append(String str) throws IOException {
        append(str, 0, str.length());
    }

    private void appendIndent(int indent) throws IOException {
        indent *= 4;
        if (indent > sSpace.length()) {
            indent = sSpace.length();
        }
        append(sSpace, 0, indent);
    }

    private void escapeAndAppendString(String string) throws IOException {
        if (string != null) {
            int N = string.length();
            char NE = (char) ESCAPE_TABLE.length;
            String[] escapes = ESCAPE_TABLE;
            int lastPos = 0;
            int pos = 0;
            while (pos < N) {
                char c = string.charAt(pos);
                if (c < NE) {
                    String escape = escapes[c];
                    if (escape != null) {
                        if (lastPos < pos) {
                            append(string, lastPos, pos - lastPos);
                        }
                        lastPos = pos + 1;
                        append(escape);
                    }
                }
                pos++;
            }
            if (lastPos < pos) {
                append(string, lastPos, pos - lastPos);
            }
        }
    }

    private void escapeAndAppendString(char[] buf, int start, int len) throws IOException {
        char NE = (char) ESCAPE_TABLE.length;
        String[] escapes = ESCAPE_TABLE;
        int end = start + len;
        int pos = start;
        int lastPos = pos;
        while (pos < end) {
            char c = buf[pos];
            if (c < NE) {
                String escape = escapes[c];
                if (escape != null) {
                    if (lastPos < pos) {
                        append(buf, lastPos, pos - lastPos);
                    }
                    lastPos = pos + 1;
                    append(escape);
                }
            }
            pos++;
        }
        if (lastPos < pos) {
            append(buf, lastPos, pos - lastPos);
        }
    }

    public XmlSerializer attribute(String namespace, String name, String value) throws IOException, IllegalArgumentException, IllegalStateException {
        append(' ');
        if (namespace != null) {
            append(namespace);
            append((char) AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        }
        append(name);
        append("=\"");
        escapeAndAppendString(value);
        append('\"');
        this.mLineStart = false;
        return this;
    }

    public void cdsect(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void comment(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void docdecl(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        flush();
    }

    public XmlSerializer endTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mNesting--;
        if (this.mInTag) {
            append(" />\n");
        } else {
            if (this.mIndent && this.mLineStart) {
                appendIndent(this.mNesting);
            }
            append("</");
            if (namespace != null) {
                append(namespace);
                append((char) AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            }
            append(name);
            append(">\n");
        }
        this.mLineStart = true;
        this.mInTag = false;
        return this;
    }

    public void entityRef(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    private void flushBytes() throws IOException {
        int position = this.mBytes.position();
        int position2 = position;
        if (position > 0) {
            this.mBytes.flip();
            this.mOutputStream.write(this.mBytes.array(), 0, position2);
            this.mBytes.clear();
        }
    }

    public void flush() throws IOException {
        if (this.mPos > 0) {
            if (this.mOutputStream != null) {
                CharBuffer charBuffer = CharBuffer.wrap(this.mText, 0, this.mPos);
                CoderResult result = this.mCharset.encode(charBuffer, this.mBytes, true);
                while (!result.isError()) {
                    if (result.isOverflow()) {
                        flushBytes();
                        result = this.mCharset.encode(charBuffer, this.mBytes, true);
                    } else {
                        flushBytes();
                        this.mOutputStream.flush();
                    }
                }
                throw new IOException(result.toString());
            }
            this.mWriter.write(this.mText, 0, this.mPos);
            this.mWriter.flush();
            this.mPos = 0;
        }
    }

    public int getDepth() {
        throw new UnsupportedOperationException();
    }

    public boolean getFeature(String name) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String getNamespace() {
        throw new UnsupportedOperationException();
    }

    public String getPrefix(String namespace, boolean generatePrefix) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public Object getProperty(String name) {
        throw new UnsupportedOperationException();
    }

    public void ignorableWhitespace(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void processingInstruction(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void setFeature(String name, boolean state) throws IllegalArgumentException, IllegalStateException {
        if (name.equals("http://xmlpull.org/v1/doc/features.html#indent-output")) {
            this.mIndent = true;
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        if (os != null) {
            try {
                this.mCharset = Charset.forName(encoding).newEncoder();
                this.mOutputStream = os;
                return;
            } catch (IllegalCharsetNameException e) {
                throw ((UnsupportedEncodingException) new UnsupportedEncodingException(encoding).initCause(e));
            } catch (UnsupportedCharsetException e2) {
                throw ((UnsupportedEncodingException) new UnsupportedEncodingException(encoding).initCause(e2));
            }
        }
        throw new IllegalArgumentException();
    }

    public void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mWriter = writer;
    }

    public void setPrefix(String prefix, String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void setProperty(String name, Object value) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version='1.0' encoding='utf-8' standalone='");
        stringBuilder.append(standalone.booleanValue() ? "yes" : "no");
        stringBuilder.append("' ?>\n");
        append(stringBuilder.toString());
        this.mLineStart = true;
    }

    public XmlSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mInTag) {
            append(">\n");
        }
        if (this.mIndent) {
            appendIndent(this.mNesting);
        }
        this.mNesting++;
        append('<');
        if (namespace != null) {
            append(namespace);
            append((char) AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        }
        append(name);
        this.mInTag = true;
        this.mLineStart = false;
        return this;
    }

    public XmlSerializer text(char[] buf, int start, int len) throws IOException, IllegalArgumentException, IllegalStateException {
        boolean z = false;
        if (this.mInTag) {
            append(">");
            this.mInTag = false;
        }
        escapeAndAppendString(buf, start, len);
        if (this.mIndent) {
            if (buf[(start + len) - 1] == 10) {
                z = true;
            }
            this.mLineStart = z;
        }
        return this;
    }

    public XmlSerializer text(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        boolean z = false;
        if (this.mInTag) {
            append(">");
            this.mInTag = false;
        }
        escapeAndAppendString(text);
        if (this.mIndent) {
            if (text.length() > 0 && text.charAt(text.length() - 1) == 10) {
                z = true;
            }
            this.mLineStart = z;
        }
        return this;
    }
}
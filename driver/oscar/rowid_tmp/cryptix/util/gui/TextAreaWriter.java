/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.gui;

import java.awt.TextArea;
import java.io.Writer;

public class TextAreaWriter
extends Writer {
    private static final long REPAINT_DELAY = 200L;
    private TextArea textArea;

    public TextAreaWriter(TextArea ta) {
        this.textArea = ta;
    }

    public void write(char[] ca, int offset, int length) {
        this.append(new String(ca, offset, length));
    }

    public void write(String s) {
        this.append(s);
    }

    public void write(String s, int offset, int length) {
        this.append(s.substring(offset, length));
    }

    public void flush() {
        this.textArea.repaint();
    }

    public void close() {
        this.flush();
    }

    private void append(String s) {
        this.textArea.append(s);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

public class RFC822Hdr {
    String name = null;
    String value = null;
    Vector subfields = null;

    public RFC822Hdr(BufferedReader rdr) throws IllegalArgumentException, IOException {
        int ch;
        StringBuffer buf = new StringBuffer();
        boolean cont = false;
        boolean first = true;
        do {
            rdr.mark(1024);
            String line = rdr.readLine();
            line = line.trim();
            if (first) {
                if (line.indexOf(58) == -1) {
                    rdr.reset();
                    break;
                }
                first = false;
            }
            if (line.length() == 0) break;
            rdr.mark(2);
            buf.append(line);
            if (line.trim().length() == 0 || (ch = rdr.read()) < 0) break;
            rdr.reset();
        } while (ch == 32 || ch == 9);
        if (buf.length() > 0) {
            this.initRFC822Hdr(buf.toString());
        }
    }

    public RFC822Hdr(String str) throws IllegalArgumentException {
        this.initRFC822Hdr(str);
    }

    private void initRFC822Hdr(String str) throws IllegalArgumentException {
        String subfield;
        int end;
        int col = str.indexOf(58);
        if (col == -1) {
            throw new IllegalArgumentException("Colon not found in header line");
        }
        this.name = str.substring(0, col).trim();
        this.value = str.substring(col + 1, str.length()).trim();
        int start = 0;
        this.subfields = new Vector();
        while ((end = this.value.indexOf(44, start)) != -1) {
            subfield = this.value.substring(start, end).trim();
            this.subfields.addElement(subfield);
            start = end + 1;
        }
        subfield = this.value.substring(start, this.value.length()).trim();
        this.subfields.addElement(subfield);
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public Vector getSubfields() {
        return this.subfields;
    }

    public String getSubfield(int index) {
        String str = (String)this.subfields.elementAt(index);
        return str;
    }
}


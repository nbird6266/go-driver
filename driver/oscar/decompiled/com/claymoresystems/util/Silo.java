/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.util;

public class Silo {
    byte[] buf;
    int first_ptr;
    int next_ptr;

    public Silo(int size) {
        this.buf = new byte[size];
        this.first_ptr = 0;
        this.next_ptr = 0;
    }

    public Silo() {
        this(1024);
    }

    public void write(byte[] in) {
        if (in.length > this.buf.length - this.next_ptr) {
            if (in.length < this.buf.length - (this.next_ptr - this.first_ptr)) {
                this.compact();
            } else {
                int newlength;
                int minlength = this.next_ptr - this.first_ptr + in.length;
                for (newlength = this.buf.length; newlength < minlength; newlength *= 2) {
                }
                byte[] tmp = new byte[newlength];
                System.arraycopy(this.buf, this.first_ptr, tmp, 0, this.next_ptr - this.first_ptr);
                this.buf = tmp;
                this.next_ptr -= this.first_ptr;
                this.first_ptr = 0;
            }
        }
        System.arraycopy(in, 0, this.buf, this.next_ptr, in.length);
        this.next_ptr += in.length;
    }

    public int read() {
        int available = this.next_ptr - this.first_ptr;
        if (available > 0) {
            return this.buf[this.first_ptr++] & 0xFF;
        }
        return -1;
    }

    public int bytesAvailable() {
        return this.next_ptr - this.first_ptr;
    }

    public int read(byte[] out, int off, int len) {
        int tocpy;
        int available = this.next_ptr - this.first_ptr;
        int n = tocpy = len > available ? available : len;
        if (available <= 0) {
            return -1;
        }
        System.arraycopy(this.buf, this.first_ptr, out, off, tocpy);
        this.first_ptr += tocpy;
        if (this.first_ptr == this.next_ptr) {
            this.compact();
        }
        return tocpy;
    }

    void compact() {
        if (this.first_ptr != 0) {
            System.arraycopy(this.buf, this.first_ptr, this.buf, 0, this.next_ptr - this.first_ptr);
            this.next_ptr -= this.first_ptr;
            this.first_ptr = 0;
        }
    }
}


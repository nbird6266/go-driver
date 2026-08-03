/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

public class EscapeTokenizer {
    private int bracesLevel = 0;
    private boolean emittingEscapeCode = false;
    private boolean inComment = false;
    private boolean inQuotes = false;
    private char lastChar = '\u0000';
    private char lastLastChar = '\u0000';
    private int pos = 0;
    private char quoteChar = '\u0000';
    private boolean sawVariableUse = false;
    private String source = null;
    private int sourceLength = 0;

    public EscapeTokenizer(String s) {
        this.source = s;
        this.sourceLength = s.length();
        this.pos = 0;
    }

    public synchronized boolean hasMoreTokens() {
        return this.pos < this.sourceLength;
    }

    public synchronized String nextToken() {
        StringBuffer tokenBuf = new StringBuffer();
        if (this.emittingEscapeCode) {
            tokenBuf.append("{");
            this.emittingEscapeCode = false;
        }
        while (this.pos < this.sourceLength) {
            block28: {
                char c;
                block29: {
                    block26: {
                        block27: {
                            c = this.source.charAt(this.pos);
                            if (!this.inQuotes && c == '@') {
                                this.sawVariableUse = true;
                            }
                            if (c != '\'' && c != '\"') break block26;
                            if (!this.inQuotes || c != this.quoteChar || this.pos + 1 >= this.sourceLength || this.source.charAt(this.pos + 1) != this.quoteChar) break block27;
                            tokenBuf.append(this.quoteChar);
                            tokenBuf.append(this.quoteChar);
                            ++this.pos;
                            break block28;
                        }
                        if (this.lastChar != '\\') {
                            if (this.inQuotes) {
                                if (this.quoteChar == c) {
                                    this.inQuotes = false;
                                }
                            } else {
                                this.inQuotes = true;
                                this.quoteChar = c;
                            }
                        } else if (this.lastLastChar == '\\') {
                            if (this.inQuotes) {
                                if (this.quoteChar == c) {
                                    this.inQuotes = false;
                                }
                            } else {
                                this.inQuotes = true;
                                this.quoteChar = c;
                            }
                        }
                        tokenBuf.append(c);
                        break block29;
                    }
                    if (c == '-') {
                        if (this.lastChar == '-' && this.lastLastChar != '\\' && !this.inQuotes) {
                            this.inComment = true;
                        }
                        tokenBuf.append(c);
                    } else if (c == '\n' || c == '\r') {
                        this.inComment = false;
                        tokenBuf.append(c);
                    } else if (c == '{') {
                        if (this.inQuotes || this.inComment) {
                            tokenBuf.append(c);
                        } else {
                            ++this.bracesLevel;
                            if (this.bracesLevel == 1) {
                                ++this.pos;
                                this.emittingEscapeCode = true;
                                return tokenBuf.toString();
                            }
                            tokenBuf.append(c);
                        }
                    } else if (c == '}') {
                        tokenBuf.append(c);
                        if (!this.inQuotes && !this.inComment) {
                            this.lastChar = c;
                            --this.bracesLevel;
                            if (this.bracesLevel == 0) {
                                ++this.pos;
                                return tokenBuf.toString();
                            }
                        }
                    } else {
                        tokenBuf.append(c);
                    }
                }
                this.lastLastChar = this.lastChar;
                this.lastChar = c;
            }
            ++this.pos;
        }
        return tokenBuf.toString();
    }

    boolean sawVariableUse() {
        return this.sawVariableUse;
    }
}


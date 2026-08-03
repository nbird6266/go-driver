/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.ASCII_CharStream;
import cryptix.asn1.lang.ParserConstants;
import cryptix.asn1.lang.Token;
import cryptix.asn1.lang.TokenMgrError;
import java.io.IOException;

public class ParserTokenManager
implements ParserConstants {
    static final long[] jjbitVec0;
    static final int[] jjnextStates;
    public static final String[] jjstrLiteralImages;
    public static final String[] lexStateNames;
    static final long[] jjtoToken;
    static final long[] jjtoSkip;
    static final long[] jjtoSpecial;
    private ASCII_CharStream input_stream;
    private final int[] jjrounds = new int[22];
    private final int[] jjstateSet = new int[44];
    protected char curChar;
    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    static {
        long[] lArray = new long[4];
        lArray[2] = -1L;
        lArray[3] = -1L;
        jjbitVec0 = lArray;
        jjnextStates = new int[]{12, 15, 17, 18, 19, 21, 1, 2, 4, 18, 19, 21, 12, 14};
        String[] stringArray = new String[59];
        stringArray[0] = "";
        stringArray[7] = "BY";
        stringArray[8] = "OF";
        stringArray[9] = "ANY";
        stringArray[10] = "BIT";
        stringArray[11] = "SET";
        stringArray[12] = "TRUE";
        stringArray[13] = "NULL";
        stringArray[14] = "OCTET";
        stringArray[15] = "FALSE";
        stringArray[16] = "STRING";
        stringArray[17] = "OBJECT";
        stringArray[18] = "BOOLEAN";
        stringArray[19] = "DEFAULT";
        stringArray[20] = "DEFINED";
        stringArray[21] = "INTEGER";
        stringArray[22] = "UTCTime";
        stringArray[23] = "PRIVATE";
        stringArray[24] = "EXPLICIT";
        stringArray[25] = "IMPLICIT";
        stringArray[26] = "OPTIONAL";
        stringArray[27] = "SEQUENCE";
        stringArray[28] = "T61String";
        stringArray[29] = "IA5String";
        stringArray[30] = "UNIVERSAL";
        stringArray[31] = "IDENTIFIER";
        stringArray[32] = "APPLICATION";
        stringArray[33] = "ISO646String";
        stringArray[34] = "NumericString";
        stringArray[35] = "TeletexString";
        stringArray[36] = "VisibleString";
        stringArray[37] = "GraphicString";
        stringArray[38] = "GeneralString";
        stringArray[39] = "VideotexString";
        stringArray[40] = "GeneralizedTime";
        stringArray[41] = "PrintableString";
        stringArray[50] = "::=";
        stringArray[51] = "{";
        stringArray[52] = "}";
        stringArray[53] = ",";
        stringArray[54] = "(";
        stringArray[55] = ")";
        stringArray[56] = "-";
        stringArray[57] = "[";
        stringArray[58] = "]";
        jjstrLiteralImages = stringArray;
        lexStateNames = new String[]{"DEFAULT"};
        jjtoToken = new long[]{575616327373291393L};
        jjtoSkip = new long[]{126L};
        jjtoSpecial = new long[]{64L};
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
            case 0: {
                if ((active0 & 0x100000000000000L) != 0L) {
                    return 0;
                }
                if ((active0 & 0x3FFFFFFFF80L) != 0L) {
                    this.jjmatchedKind = 46;
                    return 8;
                }
                return -1;
            }
            case 1: {
                if ((active0 & 0x3FFFFFFFE00L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 1;
                    return 8;
                }
                if ((active0 & 0x180L) != 0L) {
                    return 8;
                }
                return -1;
            }
            case 2: {
                if ((active0 & 0x3FFFFFFF000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 2;
                    return 8;
                }
                if ((active0 & 0xE00L) != 0L) {
                    return 8;
                }
                return -1;
            }
            case 3: {
                if ((active0 & 0x3000L) != 0L) {
                    return 8;
                }
                if ((active0 & 0x3FFFFFFC000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 3;
                    return 8;
                }
                return -1;
            }
            case 4: {
                if ((active0 & 0xC000L) != 0L) {
                    return 8;
                }
                if ((active0 & 0x3FFFFFF0000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 4;
                    return 8;
                }
                return -1;
            }
            case 5: {
                if ((active0 & 0x30000L) != 0L) {
                    return 8;
                }
                if ((active0 & 0x3FFFFFC0000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 5;
                    return 8;
                }
                return -1;
            }
            case 6: {
                if ((active0 & 0x3FFFF000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 6;
                    return 8;
                }
                if ((active0 & 0xFC0000L) != 0L) {
                    return 8;
                }
                return -1;
            }
            case 7: {
                if ((active0 & 0xF000000L) != 0L) {
                    return 8;
                }
                if ((active0 & 0x3FFF0000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 7;
                    return 8;
                }
                return -1;
            }
            case 8: {
                if ((active0 & 0x3FF80000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 8;
                    return 8;
                }
                if ((active0 & 0x70000000L) != 0L) {
                    return 8;
                }
                return -1;
            }
            case 9: {
                if ((active0 & 0x3FF00000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 9;
                    return 8;
                }
                if ((active0 & 0x80000000L) != 0L) {
                    return 8;
                }
                return -1;
            }
            case 10: {
                if ((active0 & 0x100000000L) != 0L) {
                    return 8;
                }
                if ((active0 & 0x3FE00000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 10;
                    return 8;
                }
                return -1;
            }
            case 11: {
                if ((active0 & 0x200000000L) != 0L) {
                    return 8;
                }
                if ((active0 & 0x3FC00000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 11;
                    return 8;
                }
                return -1;
            }
            case 12: {
                if ((active0 & 0x38000000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 12;
                    return 8;
                }
                if ((active0 & 0x7C00000000L) != 0L) {
                    return 8;
                }
                return -1;
            }
            case 13: {
                if ((active0 & 0x30000000000L) != 0L) {
                    this.jjmatchedKind = 46;
                    this.jjmatchedPos = 13;
                    return 8;
                }
                if ((active0 & 0x8000000000L) != 0L) {
                    return 8;
                }
                return -1;
            }
        }
        return -1;
    }

    private final int jjStartNfa_0(int pos, long active0) {
        return this.jjMoveNfa_0(this.jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private final int jjStopAtPos(int pos, int kind) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        return pos + 1;
    }

    private final int jjStartNfaWithStates_0(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            return pos + 1;
        }
        return this.jjMoveNfa_0(state, pos + 1);
    }

    private final int jjMoveStringLiteralDfa0_0() {
        switch (this.curChar) {
            case '(': {
                return this.jjStopAtPos(0, 54);
            }
            case ')': {
                return this.jjStopAtPos(0, 55);
            }
            case ',': {
                return this.jjStopAtPos(0, 53);
            }
            case '-': {
                return this.jjStartNfaWithStates_0(0, 56, 0);
            }
            case ':': {
                return this.jjMoveStringLiteralDfa1_0(0x4000000000000L);
            }
            case 'A': {
                return this.jjMoveStringLiteralDfa1_0(0x100000200L);
            }
            case 'B': {
                return this.jjMoveStringLiteralDfa1_0(263296L);
            }
            case 'D': {
                return this.jjMoveStringLiteralDfa1_0(0x180000L);
            }
            case 'E': {
                return this.jjMoveStringLiteralDfa1_0(0x1000000L);
            }
            case 'F': {
                return this.jjMoveStringLiteralDfa1_0(32768L);
            }
            case 'G': {
                return this.jjMoveStringLiteralDfa1_0(0x16000000000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa1_0(0x2A2200000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa1_0(0x400002000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa1_0(67256576L);
            }
            case 'P': {
                return this.jjMoveStringLiteralDfa1_0(0x20000800000L);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa1_0(0x8010800L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa1_0(0x810001000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa1_0(0x40400000L);
            }
            case 'V': {
                return this.jjMoveStringLiteralDfa1_0(0x9000000000L);
            }
            case '[': {
                return this.jjStopAtPos(0, 57);
            }
            case ']': {
                return this.jjStopAtPos(0, 58);
            }
            case '{': {
                return this.jjStopAtPos(0, 51);
            }
            case '}': {
                return this.jjStopAtPos(0, 52);
            }
        }
        return this.jjMoveNfa_0(5, 0);
    }

    private final int jjMoveStringLiteralDfa1_0(long active0) {
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (this.curChar) {
            case '6': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x10000000L);
            }
            case ':': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x4000000000000L);
            }
            case 'A': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x20008000L);
            }
            case 'B': {
                return this.jjMoveStringLiteralDfa2_0(active0, 131072L);
            }
            case 'C': {
                return this.jjMoveStringLiteralDfa2_0(active0, 16384L);
            }
            case 'D': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x80000000L);
            }
            case 'E': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x8180800L);
            }
            case 'F': {
                if ((active0 & 0x100L) == 0L) break;
                return this.jjStartNfaWithStates_0(1, 8, 8);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa2_0(active0, 1024L);
            }
            case 'M': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x2000000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x40200200L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa2_0(active0, 262144L);
            }
            case 'P': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x104000000L);
            }
            case 'R': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x801000L);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x200000000L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x410000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa2_0(active0, 8192L);
            }
            case 'X': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1000000L);
            }
            case 'Y': {
                if ((active0 & 0x80L) == 0L) break;
                return this.jjStartNfaWithStates_0(1, 7, 8);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa2_0(active0, 1408749273088L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x9000000000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x22000000000L);
            }
            case 'u': {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x400000000L);
            }
        }
        return this.jjStartNfa_0(0, active0);
    }

    private final int jjMoveStringLiteralDfa2_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(0, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
        switch (this.curChar) {
            case '1': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x10000000L);
            }
            case '5': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x20000000L);
            }
            case '=': {
                if ((active0 & 0x4000000000000L) == 0L) break;
                return this.jjStopAtPos(2, 50);
            }
            case 'C': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x400000L);
            }
            case 'E': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x80000000L);
            }
            case 'F': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x180000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x40800000L);
            }
            case 'J': {
                return this.jjMoveStringLiteralDfa3_0(active0, 131072L);
            }
            case 'L': {
                return this.jjMoveStringLiteralDfa3_0(active0, 40960L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x200040000L);
            }
            case 'P': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x103000000L);
            }
            case 'Q': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x8000000L);
            }
            case 'R': {
                return this.jjMoveStringLiteralDfa3_0(active0, 65536L);
            }
            case 'T': {
                if ((active0 & 0x400L) != 0L) {
                    return this.jjStartNfaWithStates_0(2, 10, 8);
                }
                if ((active0 & 0x800L) != 0L) {
                    return this.jjStartNfaWithStates_0(2, 11, 8);
                }
                return this.jjMoveStringLiteralDfa3_0(active0, 0x4204000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa3_0(active0, 4096L);
            }
            case 'Y': {
                if ((active0 & 0x200L) == 0L) break;
                return this.jjStartNfaWithStates_0(2, 9, 8);
            }
            case 'a': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x2000000000L);
            }
            case 'd': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x8000000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x20000000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x800000000L);
            }
            case 'm': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x400000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x14000000000L);
            }
            case 's': {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x1000000000L);
            }
        }
        return this.jjStartNfa_0(1, active0);
    }

    private final int jjMoveStringLiteralDfa3_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(1, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(2, active0);
            return 3;
        }
        switch (this.curChar) {
            case '6': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x200000000L);
            }
            case 'A': {
                return this.jjMoveStringLiteralDfa4_0(active0, 524288L);
            }
            case 'E': {
                if ((active0 & 0x1000L) != 0L) {
                    return this.jjStartNfaWithStates_0(3, 12, 8);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x224000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x4110000L);
            }
            case 'L': {
                if ((active0 & 0x2000L) != 0L) {
                    return this.jjStartNfaWithStates_0(3, 13, 8);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 4345561088L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x80000000L);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x30008000L);
            }
            case 'T': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x400000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x8000000L);
            }
            case 'V': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x40800000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x1CC00000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x1000000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x20000000000L);
            }
            case 'p': {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x2000000000L);
            }
        }
        return this.jjStartNfa_0(2, active0);
    }

    private final int jjMoveStringLiteralDfa4_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(2, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(3, active0);
            return 4;
        }
        switch (this.curChar) {
            case '4': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x200000000L);
            }
            case 'A': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x800000L);
            }
            case 'C': {
                return this.jjMoveStringLiteralDfa5_0(active0, 131072L);
            }
            case 'E': {
                if ((active0 & 0x8000L) != 0L) {
                    return this.jjStartNfaWithStates_0(4, 15, 8);
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x48040000L);
            }
            case 'G': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x200000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x103000000L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x110000L);
            }
            case 'O': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x4000000L);
            }
            case 'T': {
                if ((active0 & 0x4000L) != 0L) {
                    return this.jjStartNfaWithStates_0(4, 14, 8);
                }
                return this.jjMoveStringLiteralDfa5_0(active0, 0x80000000L);
            }
            case 'U': {
                return this.jjMoveStringLiteralDfa5_0(active0, 524288L);
            }
            case 'b': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x1000000000L);
            }
            case 'h': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x2000000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x400000L);
            }
            case 'o': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x8000000000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x14400000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa5_0(active0, 2234188300288L);
            }
        }
        return this.jjStartNfa_0(3, active0);
    }

    private final int jjMoveStringLiteralDfa5_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(3, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(4, active0);
            return 5;
        }
        switch (this.curChar) {
            case '6': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x200000000L);
            }
            case 'A': {
                return this.jjMoveStringLiteralDfa6_0(active0, 262144L);
            }
            case 'C': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x103000000L);
            }
            case 'E': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x300000L);
            }
            case 'G': {
                if ((active0 & 0x10000L) == 0L) break;
                return this.jjStartNfaWithStates_0(5, 16, 8);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x80000000L);
            }
            case 'L': {
                return this.jjMoveStringLiteralDfa6_0(active0, 524288L);
            }
            case 'N': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0xC000000L);
            }
            case 'R': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x40000000L);
            }
            case 'T': {
                if ((active0 & 0x20000L) != 0L) {
                    return this.jjStartNfaWithStates_0(5, 17, 8);
                }
                return this.jjMoveStringLiteralDfa6_0(active0, 0x800000L);
            }
            case 'a': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x34000000000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x800000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x2400000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x1000000000L);
            }
            case 'm': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x400000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x30000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x8000000000L);
            }
        }
        return this.jjStartNfa_0(4, active0);
    }

    private final int jjMoveStringLiteralDfa6_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(4, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(5, active0);
            return 6;
        }
        switch (this.curChar) {
            case 'A': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x104000000L);
            }
            case 'C': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x8000000L);
            }
            case 'D': {
                if ((active0 & 0x100000L) == 0L) break;
                return this.jjStartNfaWithStates_0(6, 20, 8);
            }
            case 'E': {
                if ((active0 & 0x800000L) == 0L) break;
                return this.jjStartNfaWithStates_0(6, 23, 8);
            }
            case 'F': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x80000000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x3000000L);
            }
            case 'N': {
                if ((active0 & 0x40000L) == 0L) break;
                return this.jjStartNfaWithStates_0(6, 18, 8);
            }
            case 'R': {
                if ((active0 & 0x200000L) == 0L) break;
                return this.jjStartNfaWithStates_0(6, 21, 8);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x240000000L);
            }
            case 'T': {
                if ((active0 & 0x80000L) == 0L) break;
                return this.jjStartNfaWithStates_0(6, 19, 8);
            }
            case 'b': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x20000000000L);
            }
            case 'c': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x2400000000L);
            }
            case 'e': {
                if ((active0 & 0x400000L) != 0L) {
                    return this.jjStartNfaWithStates_0(6, 22, 8);
                }
                return this.jjMoveStringLiteralDfa7_0(active0, 0x9000000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x30000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x14000000000L);
            }
            case 'x': {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x800000000L);
            }
        }
        return this.jjStartNfa_0(5, active0);
    }

    private final int jjMoveStringLiteralDfa7_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(5, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(6, active0);
            return 7;
        }
        switch (this.curChar) {
            case 'A': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x40000000L);
            }
            case 'E': {
                if ((active0 & 0x8000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(7, 27, 8);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x80000000L);
            }
            case 'L': {
                if ((active0 & 0x4000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(7, 26, 8);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x7C00000000L);
            }
            case 'T': {
                if ((active0 & 0x1000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 24, 8);
                }
                if ((active0 & 0x2000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 25, 8);
                }
                return this.jjMoveStringLiteralDfa8_0(active0, 0x100000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x10000000000L);
            }
            case 'l': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x20000000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x30000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x200000000L);
            }
            case 'x': {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x8000000000L);
            }
        }
        return this.jjStartNfa_0(6, active0);
    }

    private final int jjMoveStringLiteralDfa8_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(6, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(7, active0);
            return 8;
        }
        switch (this.curChar) {
            case 'E': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x80000000L);
            }
            case 'I': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x100000000L);
            }
            case 'L': {
                if ((active0 & 0x40000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(8, 30, 8);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x8000000000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x20000000000L);
            }
            case 'g': {
                if ((active0 & 0x10000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(8, 28, 8);
                }
                if ((active0 & 0x20000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(8, 29, 8);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x200000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x7C00000000L);
            }
            case 'z': {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x10000000000L);
            }
        }
        return this.jjStartNfa_0(7, active0);
    }

    private final int jjMoveStringLiteralDfa9_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(7, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(8, active0);
            return 9;
        }
        switch (this.curChar) {
            case 'O': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x100000000L);
            }
            case 'R': {
                if ((active0 & 0x80000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(9, 31, 8);
            }
            case 'S': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x20000000000L);
            }
            case 'e': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x10000000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x200000000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x7C00000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa10_0(active0, 0x8000000000L);
            }
        }
        return this.jjStartNfa_0(8, active0);
    }

    private final int jjMoveStringLiteralDfa10_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(8, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(9, active0);
            return 10;
        }
        switch (this.curChar) {
            case 'N': {
                if ((active0 & 0x100000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(10, 32, 8);
            }
            case 'd': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x10000000000L);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x7C00000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x200000000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x8000000000L);
            }
            case 't': {
                return this.jjMoveStringLiteralDfa11_0(active0, 0x20000000000L);
            }
        }
        return this.jjStartNfa_0(9, active0);
    }

    private final int jjMoveStringLiteralDfa11_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(9, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(10, active0);
            return 11;
        }
        switch (this.curChar) {
            case 'T': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x10000000000L);
            }
            case 'g': {
                if ((active0 & 0x200000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(11, 33, 8);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x8000000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x7C00000000L);
            }
            case 'r': {
                return this.jjMoveStringLiteralDfa12_0(active0, 0x20000000000L);
            }
        }
        return this.jjStartNfa_0(10, active0);
    }

    private final int jjMoveStringLiteralDfa12_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(10, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(11, active0);
            return 12;
        }
        switch (this.curChar) {
            case 'g': {
                if ((active0 & 0x400000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(12, 34, 8);
                }
                if ((active0 & 0x800000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(12, 35, 8);
                }
                if ((active0 & 0x1000000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(12, 36, 8);
                }
                if ((active0 & 0x2000000000L) != 0L) {
                    return this.jjStartNfaWithStates_0(12, 37, 8);
                }
                if ((active0 & 0x4000000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(12, 38, 8);
            }
            case 'i': {
                return this.jjMoveStringLiteralDfa13_0(active0, 0x30000000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa13_0(active0, 0x8000000000L);
            }
        }
        return this.jjStartNfa_0(11, active0);
    }

    private final int jjMoveStringLiteralDfa13_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(11, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(12, active0);
            return 13;
        }
        switch (this.curChar) {
            case 'g': {
                if ((active0 & 0x8000000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(13, 39, 8);
            }
            case 'm': {
                return this.jjMoveStringLiteralDfa14_0(active0, 0x10000000000L);
            }
            case 'n': {
                return this.jjMoveStringLiteralDfa14_0(active0, 0x20000000000L);
            }
        }
        return this.jjStartNfa_0(12, active0);
    }

    private final int jjMoveStringLiteralDfa14_0(long old0, long active0) {
        if ((active0 &= old0) == 0L) {
            return this.jjStartNfa_0(12, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        }
        catch (IOException e) {
            this.jjStopStringLiteralDfa_0(13, active0);
            return 14;
        }
        switch (this.curChar) {
            case 'e': {
                if ((active0 & 0x10000000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(14, 40, 8);
            }
            case 'g': {
                if ((active0 & 0x20000000000L) == 0L) break;
                return this.jjStartNfaWithStates_0(14, 41, 8);
            }
        }
        return this.jjStartNfa_0(13, active0);
    }

    private final void jjCheckNAdd(int state) {
        if (this.jjrounds[state] != this.jjround) {
            this.jjstateSet[this.jjnewStateCnt++] = state;
            this.jjrounds[state] = this.jjround;
        }
    }

    private final void jjAddStates(int start, int end) {
        do {
            this.jjstateSet[this.jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private final void jjCheckNAddTwoStates(int state1, int state2) {
        this.jjCheckNAdd(state1);
        this.jjCheckNAdd(state2);
    }

    private final void jjCheckNAddStates(int start, int end) {
        do {
            this.jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    private final void jjCheckNAddStates(int start) {
        this.jjCheckNAdd(jjnextStates[start]);
        this.jjCheckNAdd(jjnextStates[start + 1]);
    }

    private final int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 22;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            if (++this.jjround == Integer.MAX_VALUE) {
                this.ReInitRounds();
            }
            if (this.curChar < '@') {
                long l = 1L << this.curChar;
                block40: do {
                    switch (this.jjstateSet[--i]) {
                        case 5: {
                            if ((0x3FF000000000000L & l) != 0L) {
                                if (kind > 42) {
                                    kind = 42;
                                }
                                this.jjCheckNAdd(6);
                                break;
                            }
                            if (this.curChar == '\'') {
                                this.jjCheckNAddStates(0, 5);
                                break;
                            }
                            if (this.curChar != '-') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 0;
                            break;
                        }
                        case 0: {
                            if (this.curChar != '-') break;
                            this.jjCheckNAddStates(6, 8);
                            break;
                        }
                        case 1: {
                            if ((0xFFFFFFFFFFFFDBFFL & l) == 0L) break;
                            this.jjCheckNAddStates(6, 8);
                            break;
                        }
                        case 2: {
                            if ((0x2400L & l) == 0L || kind <= 6) continue block40;
                            kind = 6;
                            break;
                        }
                        case 3: {
                            if (this.curChar != '\n' || kind <= 6) continue block40;
                            kind = 6;
                            break;
                        }
                        case 4: {
                            if (this.curChar != '\r') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 3;
                            break;
                        }
                        case 6: {
                            if ((0x3FF000000000000L & l) == 0L) continue block40;
                            if (kind > 42) {
                                kind = 42;
                            }
                            this.jjCheckNAdd(6);
                            break;
                        }
                        case 8: {
                            if ((0x3FF200000000000L & l) == 0L) continue block40;
                            if (kind > 46) {
                                kind = 46;
                            }
                            this.jjstateSet[this.jjnewStateCnt++] = 8;
                            break;
                        }
                        case 10: {
                            if ((0x3FF200000000000L & l) == 0L) continue block40;
                            if (kind > 47) {
                                kind = 47;
                            }
                            this.jjstateSet[this.jjnewStateCnt++] = 10;
                            break;
                        }
                        case 11: {
                            if (this.curChar != '\'') break;
                            this.jjCheckNAddStates(0, 5);
                            break;
                        }
                        case 12: {
                            if ((0x3FF000000000000L & l) == 0L) break;
                            this.jjCheckNAddTwoStates(12, 14);
                            break;
                        }
                        case 14: {
                            if (this.curChar != '\'') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 13;
                            break;
                        }
                        case 15: {
                            if ((0x3000000000000L & l) == 0L) break;
                            this.jjCheckNAddTwoStates(15, 17);
                            break;
                        }
                        case 17: {
                            if (this.curChar != '\'') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 16;
                            break;
                        }
                        case 18: {
                            if ((0xFFFFFFFBFFFFDBFFL & l) == 0L) break;
                            this.jjCheckNAddStates(9, 11);
                            break;
                        }
                        case 20: {
                            if ((0x8400000000L & l) == 0L) break;
                            this.jjCheckNAddStates(9, 11);
                            break;
                        }
                        case 21: {
                            if (this.curChar != '\'' || kind <= 45) continue block40;
                            kind = 45;
                            break;
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < '\u0080') {
                long l = 1L << (this.curChar & 0x3F);
                block41: do {
                    switch (this.jjstateSet[--i]) {
                        case 5: {
                            if ((0x7FFFFFE00000000L & l) != 0L) {
                                if (kind > 47) {
                                    kind = 47;
                                }
                                this.jjCheckNAdd(10);
                                break;
                            }
                            if ((0x7FFFFFEL & l) == 0L) break;
                            if (kind > 46) {
                                kind = 46;
                            }
                            this.jjCheckNAdd(8);
                            break;
                        }
                        case 1: {
                            this.jjAddStates(6, 8);
                            break;
                        }
                        case 7: {
                            if ((0x7FFFFFEL & l) == 0L) continue block41;
                            if (kind > 46) {
                                kind = 46;
                            }
                            this.jjCheckNAdd(8);
                            break;
                        }
                        case 8: {
                            if ((0x7FFFFFE07FFFFFEL & l) == 0L) continue block41;
                            if (kind > 46) {
                                kind = 46;
                            }
                            this.jjCheckNAdd(8);
                            break;
                        }
                        case 9: {
                            if ((0x7FFFFFE00000000L & l) == 0L) continue block41;
                            if (kind > 47) {
                                kind = 47;
                            }
                            this.jjCheckNAdd(10);
                            break;
                        }
                        case 10: {
                            if ((0x7FFFFFE07FFFFFEL & l) == 0L) continue block41;
                            if (kind > 47) {
                                kind = 47;
                            }
                            this.jjCheckNAdd(10);
                            break;
                        }
                        case 12: {
                            if ((0x7E0000007EL & l) == 0L) break;
                            this.jjAddStates(12, 13);
                            break;
                        }
                        case 13: {
                            if (this.curChar != 'H' || kind <= 43) continue block41;
                            kind = 43;
                            break;
                        }
                        case 16: {
                            if (this.curChar != 'B' || kind <= 44) continue block41;
                            kind = 44;
                            break;
                        }
                        case 18: {
                            if ((0xFFFFFFFFEFFFFFFFL & l) == 0L) break;
                            this.jjCheckNAddStates(9, 11);
                            break;
                        }
                        case 19: {
                            if (this.curChar != '\\') break;
                            this.jjstateSet[this.jjnewStateCnt++] = 20;
                            break;
                        }
                        case 20: {
                            if ((0x14404410000000L & l) == 0L) break;
                            this.jjCheckNAddStates(9, 11);
                            break;
                        }
                    }
                } while (i != startsAt);
            } else {
                int i2 = (this.curChar & 0xFF) >> 6;
                long l2 = 1L << (this.curChar & 0x3F);
                do {
                    switch (this.jjstateSet[--i]) {
                        case 1: {
                            if ((jjbitVec0[i2] & l2) == 0L) break;
                            this.jjAddStates(6, 8);
                            break;
                        }
                        case 18: {
                            if ((jjbitVec0[i2] & l2) == 0L) break;
                            this.jjAddStates(9, 11);
                            break;
                        }
                    }
                } while (i != startsAt);
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            if (i == (startsAt = 22 - this.jjnewStateCnt)) {
                return curPos;
            }
            try {
                this.curChar = this.input_stream.readChar();
            }
            catch (IOException e) {
                return curPos;
            }
        }
    }

    public ParserTokenManager(ASCII_CharStream stream) {
        this.input_stream = stream;
    }

    public ParserTokenManager(ASCII_CharStream stream, int lexState) {
        this(stream);
        this.SwitchTo(lexState);
    }

    public void ReInit(ASCII_CharStream stream) {
        this.jjnewStateCnt = 0;
        this.jjmatchedPos = 0;
        this.curLexState = this.defaultLexState;
        this.input_stream = stream;
        this.ReInitRounds();
    }

    private final void ReInitRounds() {
        this.jjround = -2147483647;
        int i = 22;
        while (i-- > 0) {
            this.jjrounds[i] = Integer.MIN_VALUE;
        }
    }

    public void ReInit(ASCII_CharStream stream, int lexState) {
        this.ReInit(stream);
        this.SwitchTo(lexState);
    }

    public void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2);
        }
        this.curLexState = lexState;
    }

    private final Token jjFillToken() {
        Token t = Token.newToken(this.jjmatchedKind);
        t.kind = this.jjmatchedKind;
        String im = jjstrLiteralImages[this.jjmatchedKind];
        t.image = im == null ? this.input_stream.GetImage() : im;
        t.beginLine = this.input_stream.getBeginLine();
        t.beginColumn = this.input_stream.getBeginColumn();
        t.endLine = this.input_stream.getEndLine();
        t.endColumn = this.input_stream.getEndColumn();
        return t;
    }

    /*
     * Unable to fully structure code
     */
    public final Token getNextToken() {
        specialToken = null;
        curPos = 0;
        while (true) {
            try {
                this.curChar = this.input_stream.BeginToken();
                if (true) ** GOTO lbl14
            }
            catch (IOException e) {
                this.jjmatchedKind = 0;
                matchedToken = this.jjFillToken();
                matchedToken.specialToken = specialToken;
                return matchedToken;
            }
            {
                do {
                    this.curChar = this.input_stream.BeginToken();
lbl14:
                    // 2 sources

                } while (this.curChar <= ' ' && (4294981120L & 1L << this.curChar) != 0L);
            }
            this.jjmatchedKind = 0x7FFFFFFF;
            this.jjmatchedPos = 0;
            curPos = this.jjMoveStringLiteralDfa0_0();
            if (this.jjmatchedKind == 0x7FFFFFFF) break;
            if (this.jjmatchedPos + 1 < curPos) {
                this.input_stream.backup(curPos - this.jjmatchedPos - 1);
            }
            if ((ParserTokenManager.jjtoToken[this.jjmatchedKind >> 6] & 1L << (this.jjmatchedKind & 63)) != 0L) {
                matchedToken = this.jjFillToken();
                matchedToken.specialToken = specialToken;
                return matchedToken;
            }
            if ((ParserTokenManager.jjtoSpecial[this.jjmatchedKind >> 6] & 1L << (this.jjmatchedKind & 63)) == 0L) continue;
            matchedToken = this.jjFillToken();
            if (specialToken == null) {
                specialToken = matchedToken;
                continue;
            }
            matchedToken.specialToken = specialToken;
            specialToken = specialToken.next = matchedToken;
        }
        error_line = this.input_stream.getEndLine();
        error_column = this.input_stream.getEndColumn();
        error_after = null;
        EOFSeen = false;
        try {
            this.input_stream.readChar();
            this.input_stream.backup(1);
        }
        catch (IOException e1) {
            EOFSeen = true;
            v0 = error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
            if (this.curChar == '\n' || this.curChar == '\r') {
                ++error_line;
                error_column = 0;
            }
            ++error_column;
        }
        if (!EOFSeen) {
            this.input_stream.backup(1);
            error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
        }
        throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0);
    }
}


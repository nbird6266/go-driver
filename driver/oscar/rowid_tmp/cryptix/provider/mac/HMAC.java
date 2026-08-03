/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.mac;

import cryptix.CryptixException;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import xjava.security.Parameterized;

public class HMAC
extends MessageDigest
implements Parameterized,
Cloneable {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("HMAC");
    private static final PrintWriter err = Debug.getOutput();
    private MessageDigest md;
    private int blockSize;
    private int length;
    private byte[] key;

    private static void debug(String s) {
        err.println("HMAC: " + s);
    }

    public HMAC(String mdAlgorithm, int mdBlockSize) {
        super("HMAC-" + mdAlgorithm);
        try {
            this.md = MessageDigest.getInstance(mdAlgorithm, "Cryptix");
        }
        catch (Exception e) {
            try {
                this.md = MessageDigest.getInstance(mdAlgorithm);
            }
            catch (Exception e2) {
                throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Unable to instantiate the " + mdAlgorithm + " MessageDigest\n" + e2);
            }
        }
        this.blockSize = mdBlockSize;
        this.length = this.md.digest().length;
        this.key = new byte[mdBlockSize];
    }

    private HMAC(HMAC mac) throws CloneNotSupportedException {
        super(mac.getAlgorithm());
        this.md = (MessageDigest)mac.md.clone();
        this.blockSize = mac.blockSize;
        this.length = mac.length;
        byte[] _key = mac.key;
        this.key = _key == null ? null : (byte[])_key.clone();
    }

    public Object clone() throws CloneNotSupportedException {
        return new HMAC(this);
    }

    protected void engineReset() {
        this.md.reset();
        this.key = null;
    }

    protected void engineUpdate(byte input) {
        if (this.key == null) {
            throw new IllegalStateException(String.valueOf(this.getAlgorithm()) + ": Key has not been set");
        }
        this.md.update(input);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        if (this.key == null) {
            throw new IllegalStateException(String.valueOf(this.getAlgorithm()) + ": Key has not been set");
        }
        this.md.update(input, offset, len);
    }

    protected byte[] engineDigest() {
        if (debuglevel >= 7) {
            HMAC.debug("engineDigest()");
        }
        if (this.key == null) {
            throw new IllegalStateException(String.valueOf(this.getAlgorithm()) + ": Key has not been set");
        }
        byte[] innerhash = this.md.digest();
        byte[] xorblock = new byte[this.blockSize];
        int i = 0;
        while (i < this.key.length) {
            xorblock[i] = (byte)(0x5C ^ this.key[i]);
            ++i;
        }
        i = this.key.length;
        while (i < this.blockSize) {
            xorblock[i] = 92;
            ++i;
        }
        this.md.update(xorblock);
        byte[] result = this.md.digest(innerhash);
        if (debuglevel >= 7) {
            HMAC.debug("... = <" + Hex.toString(result) + ">");
        }
        return result;
    }

    protected int engineGetDigestLength() {
        return this.length;
    }

    public void setParameter(String param, Object value) throws InvalidParameterException {
        this.engineSetParameter(param, value);
    }

    public Object getParameter(String param) throws InvalidParameterException {
        return this.engineGetParameter(param);
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        try {
            if (param.equalsIgnoreCase("key")) {
                this.setKey((byte[])value);
                return;
            }
            if (this.key != null) {
                throw new InvalidParameterException(String.valueOf(this.getAlgorithm()) + ": Can't set parameter after key has been initialised");
            }
            ((Parameterized)((Object)this.md)).setParameter(param, value);
        }
        catch (Exception e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    protected Object engineGetParameter(String param) throws InvalidParameterException {
        try {
            return ((Parameterized)((Object)this.md)).getParameter(param);
        }
        catch (Exception e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    private void setKey(byte[] userkey) {
        if (debuglevel >= 7) {
            HMAC.debug("setKey(<" + Hex.toString(userkey) + ">)");
        }
        if (this.key != null) {
            this.md.reset();
        }
        this.key = userkey.length > this.blockSize ? this.md.digest(userkey) : (byte[])userkey.clone();
        byte[] xorblock = new byte[this.blockSize];
        int i = 0;
        while (i < this.key.length) {
            xorblock[i] = (byte)(0x36 ^ this.key[i]);
            ++i;
        }
        i = this.key.length;
        while (i < this.blockSize) {
            xorblock[i] = 54;
            ++i;
        }
        this.md.update(xorblock);
    }
}


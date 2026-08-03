/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.CryptixProperties;
import cryptix.provider.elgamal.BaseElGamalParams;
import cryptix.provider.elgamal.BaseElGamalPrivateKey;
import cryptix.provider.elgamal.BaseElGamalPublicKey;
import cryptix.provider.elgamal.DefaultElGamalParameterSet;
import cryptix.provider.elgamal.GenericElGamalParameterSet;
import cryptix.util.core.Debug;
import cryptix.util.math.Prime;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import xjava.security.interfaces.ElGamalKeyPairGenerator;
import xjava.security.interfaces.ElGamalParams;

public class BaseElGamalKeyPairGenerator
extends KeyPairGenerator
implements ElGamalKeyPairGenerator {
    private static final boolean DEBUG = true;
    private static final int debuglevel;
    private static final PrintWriter err;
    private static final int CONFIDENCE = 80;
    private static final boolean USE_PRECOMPUTED = true;
    private static final boolean USE_SMALL_G = false;
    private static final int PRIME_TYPE = 1;
    private static final int MIN_PRIME_LEN = 256;
    private static final BigInteger ZERO;
    private static final BigInteger ONE;
    private static GenericElGamalParameterSet defaultParamSet;
    protected BigInteger p;
    protected BigInteger g;
    protected SecureRandom source;
    private static BigInteger[] efficientBases;

    static {
        block4: {
            debuglevel = Debug.getLevel("ElGamal", "BaseElGamalKeyPairGenerator");
            err = Debug.getOutput();
            ZERO = BigInteger.valueOf(0L);
            ONE = BigInteger.valueOf(1L);
            try {
                String classname = CryptixProperties.getProperty("Alg.DefaultParameterSet.ElGamal");
                if (classname != null) {
                    defaultParamSet = (GenericElGamalParameterSet)Class.forName(classname).newInstance();
                }
            }
            catch (Exception e) {
                if (debuglevel < 1) break block4;
                BaseElGamalKeyPairGenerator.debug("exception while instantiating default parameter set: " + e);
            }
        }
        if (defaultParamSet == null) {
            defaultParamSet = new DefaultElGamalParameterSet();
        }
    }

    private static void debug(String s) {
        err.println("BaseElGamalKeyPairGenerator: " + s);
    }

    private static void progress(String s) {
        err.print(s);
        err.flush();
    }

    public BaseElGamalKeyPairGenerator() {
        super("ElGamal");
    }

    public void initialize(ElGamalParams params, SecureRandom random) throws InvalidParameterException {
        this.initialize(params.getP(), params.getG(), random);
    }

    public void initialize(BigInteger prime, BigInteger base, SecureRandom random) throws InvalidParameterException {
        if (prime == null) {
            throw new NullPointerException("prime == null");
        }
        if (base == null) {
            throw new NullPointerException("base == null");
        }
        if (random == null) {
            throw new NullPointerException("random == null");
        }
        if (base.compareTo(prime) >= 0) {
            throw new InvalidParameterException("base >= prime");
        }
        this.p = prime;
        this.g = base;
        this.source = random;
    }

    public void initialize(int primeLen, SecureRandom random) {
        ElGamalParams params = null;
        if (defaultParamSet != null) {
            params = defaultParamSet.getParameters(primeLen);
        }
        if (params == null) {
            params = this.generateParams(primeLen, random);
        }
        this.p = params.getP();
        this.g = params.getG();
        this.source = random;
    }

    public void initialize(int primeLen, boolean genParams, SecureRandom random) throws InvalidParameterException {
        ElGamalParams params;
        if (primeLen < 256) {
            throw new InvalidParameterException("ElGamal: prime length " + primeLen + " is too short (< " + 256 + ")");
        }
        if (genParams || defaultParamSet == null) {
            params = this.generateParams(primeLen, random);
        } else {
            params = defaultParamSet.getParameters(primeLen);
            if (params == null) {
                throw new InvalidParameterException("ElGamal: no pre-computed parameters for prime length " + primeLen);
            }
        }
        this.p = params.getP();
        this.g = params.getG();
        this.source = random;
    }

    public KeyPair generateKeyPair() {
        if (this.p == null) {
            throw new CryptixException("ElGamal: key pair generator not initialized");
        }
        int length = this.p.bitLength() - 1;
        BigInteger x = new BigInteger(length, this.source).setBit(length);
        BaseElGamalPrivateKey privateKey = new BaseElGamalPrivateKey(this.p, this.g, x);
        BaseElGamalPublicKey publicKey = new BaseElGamalPublicKey(this.p, this.g, privateKey.getY());
        return new KeyPair(publicKey, privateKey);
    }

    public ElGamalParams generateParams(int primeLen, SecureRandom random) throws InvalidParameterException {
        if (primeLen < 256) {
            throw new InvalidParameterException("ElGamal: prime length " + primeLen + " is too short (< " + 256 + ")");
        }
        Object[] result = Prime.getElGamal(primeLen, 80, random, 1);
        BigInteger newP = (BigInteger)result[0];
        BigInteger[] q = (BigInteger[])result[1];
        BigInteger newG = BaseElGamalKeyPairGenerator.findG(newP, q, random);
        return new BaseElGamalParams(newP, newG);
    }

    private static BigInteger findG(BigInteger p, BigInteger[] q, SecureRandom random) {
        BigInteger g;
        BigInteger p_minus_1 = p.subtract(ONE);
        BigInteger[] z = new BigInteger[q.length];
        int i = 0;
        while (i < q.length) {
            z[i] = p_minus_1.divide(q[i]);
            ++i;
        }
        if (debuglevel >= 5) {
            BaseElGamalKeyPairGenerator.progress("g =");
        }
        int length = p.bitLength() - 1;
        do {
            if (debuglevel < 5) continue;
            BaseElGamalKeyPairGenerator.progress(" ?");
        } while (!Prime.isGeneratorModP(g = new BigInteger(length, random).setBit(length), p, z));
        if (debuglevel >= 4) {
            err.println(" OK");
        }
        return g;
    }
}


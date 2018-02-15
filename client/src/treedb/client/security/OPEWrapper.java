package treedb.client.security;

import java.math.BigInteger;

import ch.ethz.inf.dsg.crypto.OPE;

public class OPEWrapper {
    private byte[] key;
    private OPE ope;

    public OPEWrapper() {
        key = new byte[16];
        ope = new OPE(this.key, 64, 128);
    }

    public BigInteger encrypt(BigInteger val) {
        return ope.encrypt(val);
    }

    public BigInteger decrypt(BigInteger val) {
        return ope.decrypt(val);
    }
}
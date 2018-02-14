package treedb.client.security;

import ch.ethz.dsg.ore.ORE;
import ch.ethz.dsg.ore.ORE.ORECiphertext;
import ch.ethz.dsg.ore.ORE.OREKey;
import java.math.BigInteger;
import java.util.Base64;

public class OREWrapper {

    private OREKey key;
    private ORE instance;

    public OREWrapper() {
        this.key = ORE.generateKey();
        this.instance = ORE.getDefaultOREInstance(key);
    }

    public OREWrapper(OREKey key) {
        this.key = key;
        this.instance = ORE.getDefaultOREInstance(key);
    }

    public OREKey getKey() {
        return this.key;
    }

    public String encryptAndEncode(BigInteger val) throws Exception {
        ORECiphertext ciphertext = instance.encrypt(val.longValueExact());
        return Base64.getEncoder().encodeToString(ciphertext.encode());
    }

    public BigInteger decodeAndDecrypt(String encodedCiphertext) throws Exception {
        ORECiphertext ciphertext = ORECiphertext.decodeDefault(Base64.getDecoder().decode(encodedCiphertext));
        return BigInteger.valueOf(this.instance.decrypt(ciphertext));
    }
}
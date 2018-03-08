package timecrypt.client.security;

import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import java.math.BigInteger;

public class PaillierWrapper {

    private PaillierPrivateKey privateKey;
    private PaillierPublicKey publicKey;

    public PaillierWrapper() {
        privateKey = PaillierPrivateKey.create(3072);
        publicKey = privateKey.getPublicKey();
    }

    public PaillierWrapper(PaillierPrivateKey pKey, PaillierPublicKey pubKey) {
        privateKey = pKey;
        publicKey = pubKey;
    }
    
    public PaillierPublicKey getPublicKey() {
        return this.publicKey;
    }

    public BigInteger encrypt(BigInteger val) {
        return this.publicKey.raw_encrypt(val);
    }

    public BigInteger decrypt(BigInteger val) {
        return this.privateKey.raw_decrypt(val);
    }
}
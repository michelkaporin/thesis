package treedb.client.security;

import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;

public class CryptoKeyPair {

    public PaillierPrivateKey privateKey;
    public PaillierPublicKey publicKey;

    public CryptoKeyPair(PaillierPrivateKey pKey, PaillierPublicKey pubKey) {
        privateKey = pKey;
        publicKey = pubKey;
    }

    public static CryptoKeyPair generateKeyPair() {
        PaillierPrivateKey pKey = PaillierPrivateKey.create(2048);
        PaillierPublicKey pubKey = pKey.getPublicKey();

		return new CryptoKeyPair(pKey, pubKey);
    }
}
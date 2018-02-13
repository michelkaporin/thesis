package treedb.client.security;

import java.math.BigInteger;
import java.util.Base64;
import ch.ethz.dsg.ecelgamal.ECElGamal;
import ch.ethz.dsg.ecelgamal.ECElGamal.CRTParams;
import ch.ethz.dsg.ecelgamal.ECElGamal.ECElGamalCiphertext;

public class ECElGamalWrapper {

    private ECElGamal.ECElGamalKey key;

    public ECElGamalWrapper() {
        CRTParams params32 = ECElGamal.getDefault32BitParams();
        this.key = ECElGamal.generateNewKey(params32);
    }

    public ECElGamalWrapper(ECElGamal.ECElGamalKey key) {
        this.key = key;
    }

    public String encryptAndEncode(BigInteger val) {
        ECElGamalCiphertext ciphertext = ECElGamal.encrypt(val, this.key);
        return Base64.getEncoder().encodeToString(ciphertext.encode());
    }

    public BigInteger decodeAndDecrypt(String encodedCipherText) {
        ECElGamalCiphertext ciphertext = ECElGamalCiphertext.decode(Base64.getDecoder().decode(encodedCipherText));
        int decrypted = ECElGamal.decrypt32(ciphertext, this.key); // Would be great to have it represented as BigInteger to match Paillier implementation

        return BigInteger.valueOf(decrypted);
    }

    public ECElGamal.ECElGamalKey getKey() {
        return this.key;
    }
}
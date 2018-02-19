package treedb.client.security;

import java.math.BigInteger;
import java.util.Base64;
import ch.ethz.dsg.ecelgamal.ECElGamal;
import ch.ethz.dsg.ecelgamal.ECElGamal.CRTParams;
import ch.ethz.dsg.ecelgamal.ECElGamal.ECElGamalCiphertext;
import ch.ethz.dsg.ecelgamal.ECElGamal.ECElGamalKey;

public class ECElGamalWrapper {

    private ECElGamalKey key;

    public ECElGamalWrapper() {
        CRTParams params32 = ECElGamal.getDefault32BitParams();
        this.key = ECElGamal.generateNewKey(params32);
        ECElGamal.initBsgsTable(65536);
    }

    public ECElGamalWrapper(ECElGamalKey key) {
        this.key = key;
    }

    public String encryptAndEncode(BigInteger val) {
        ECElGamalCiphertext ciphertext = ECElGamal.encrypt(val, this.key);
        return Base64.getEncoder().encodeToString(ciphertext.encode());
    }

    public BigInteger decodeAndDecrypt(String encodedCiphertext) {
        ECElGamalCiphertext ciphertext = ECElGamalCiphertext.decode(Base64.getDecoder().decode(encodedCiphertext));
        int decrypted = ECElGamal.decrypt32(ciphertext, this.key); // Would be great to have it represented as BigInteger to match Paillier implementation

        return BigInteger.valueOf(decrypted);
    }

    public ECElGamalKey getKey() {
        return this.key;
    }
}
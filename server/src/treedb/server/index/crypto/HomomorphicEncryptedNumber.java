package treedb.server.index.crypto;

import ch.ethz.dsg.ecelgamal.ECElGamal;
import ch.ethz.dsg.ecelgamal.ECElGamal.ECElGamalCiphertext;
import com.n1analytics.paillier.EncryptedNumber;
import java.util.Base64;

public class HomomorphicEncryptedNumber {

    private Object number;

    public HomomorphicEncryptedNumber(Object number) {
        if (number instanceof EncryptedNumber || number instanceof ECElGamalCiphertext) {
            this.number = number;
        } else {
            throw new RuntimeException("Number provided is neither Paillier, nor ECELGamal encrypted.");
        }
    }

    public Object getValue() {
        return this.number;
    }

    public boolean isPaillierEncrypted() {
        if (this.number instanceof EncryptedNumber) {
            return true;
        }

        return false;
    }

    public boolean isECELGamalEncrypted() {
        if (this.number instanceof ECElGamalCiphertext) {
            return true;
        }
        
        return false;
    }

    public HomomorphicEncryptedNumber add(HomomorphicEncryptedNumber number) {
        if (!sameTypes(number)) {
            throw new RuntimeException("Objects have different types, summation is not possible.");
        }

        if (isPaillierEncrypted()) {
            return new HomomorphicEncryptedNumber(((EncryptedNumber) this.number).add((EncryptedNumber) number.getValue()));
        } 
        
        return new HomomorphicEncryptedNumber(ECElGamal.add((ECElGamalCiphertext) this.number, (ECElGamalCiphertext) number.getValue()));
    }

    public String toString() {
        if (isPaillierEncrypted()) {
            return ((EncryptedNumber) this.number).calculateCiphertext().toString();
        }
        
        return "\"" + Base64.getEncoder().encodeToString(((ECElGamalCiphertext) this.number).encode()) + "\"";
    }

    private boolean sameTypes(HomomorphicEncryptedNumber number) {
        if (number.getValue().getClass().equals(this.number.getClass())) {
            return true;
        }

        return false;
    }
}
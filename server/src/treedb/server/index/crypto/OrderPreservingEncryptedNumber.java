package treedb.server.index.crypto;

import java.math.BigInteger;
import java.util.Base64;
import ch.ethz.dsg.ore.ORE.ORECiphertext;

public class OrderPreservingEncryptedNumber implements EncryptedNumber {
    private Object number;

    public OrderPreservingEncryptedNumber(Object number) {
        if (number instanceof BigInteger || number instanceof ORECiphertext) {
            this.number = number;
        } else {
            throw new RuntimeException("Number provided is neither OPE, nor ORE encrypted.");
        }
    }

    public Object getValue() {
        return this.number;
    }

    public OrderPreservingEncryptedNumber min(OrderPreservingEncryptedNumber number) {
        return this.compare(number, -1);
    }

    public OrderPreservingEncryptedNumber max(OrderPreservingEncryptedNumber number) {
        return this.compare(number, 1);
    }

    /**
     * min = -1;
     * max = 1;
     */
    private OrderPreservingEncryptedNumber compare(OrderPreservingEncryptedNumber number, int compare) {
        if (!sameTypes(number)) {
            throw new RuntimeException("Objects have different types, summation is not possible.");
        }
        if (isOPEEncrypted()) {
            if (((BigInteger) this.number).compareTo((BigInteger) number.getValue()) == compare) {
                return this;
            }
            return number;
        }
        if (((ORECiphertext) this.number).compareTo((ORECiphertext) number.getValue()) == compare) {
            return this;
        }
        return number;
    }

    public String toJsonString() {
        if (isOPEEncrypted()) {
            return ((BigInteger) this.number).toString();
        }
        
        return "\"" + Base64.getEncoder().encodeToString(((ORECiphertext) this.number).encode()) + "\"";
    }

    public boolean isOPEEncrypted() {
        if (this.number instanceof BigInteger) {
            return true;
        }
        return false;
    }

    private boolean sameTypes(OrderPreservingEncryptedNumber number) {
        if (number.getValue().getClass().equals(this.number.getClass())) {
            return true;
        }

        return false;
    }
}
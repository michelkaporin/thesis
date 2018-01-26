package treedb.server.utils;

import java.math.BigInteger;
import java.util.UUID;
import com.n1analytics.paillier.PaillierPublicKey;

public class Utility {
    public static byte[] byteArrayStringToByteArray(String byteArray) {
        String[] byteValues = byteArray.substring(1, byteArray.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0, len = bytes.length; i < len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }

        return bytes;
    }

    public static UUID UUIDFromString(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static PaillierPublicKey unmarshalPublicKey(String modulus) {
        return new PaillierPublicKey(new BigInteger(modulus));
    }
}
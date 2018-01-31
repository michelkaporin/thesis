package treedb.server.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.UUID;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    public static BitSet unmarshalBitSet(JsonArray bitset) {
        ArrayList<Long> setBits = new ArrayList<>();
        Iterator<JsonElement> it = bitset.iterator();
        while (it.hasNext()) {
            setBits.add(it.next().getAsLong());
        }
        setBits.toArray();
        long[] bits = new long[setBits.size()];

        for (int i = 0; i < setBits.size(); i++) {
            bits[i] = setBits.get(i).longValue();
        }
        return BitSet.valueOf(bits);
    }

    public static void mergeBitSet(BitSet fromBitSet, BitSet toBitSet) {
        int nextBit = fromBitSet.nextSetBit(0);
        while (nextBit != -1) {
            toBitSet.set(nextBit);
            nextBit = fromBitSet.nextSetBit(nextBit+1);
        }
    }
}
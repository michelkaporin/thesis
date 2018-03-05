package timecrypt.server.utils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.n1analytics.paillier.PaillierPublicKey;

public class Utility {
    public static Encoder base64Encoder = Base64.getEncoder();
    public static Decoder base64Decoder = Base64.getDecoder();

    public static String encodeBase64(byte[] data) {
        return base64Encoder.encodeToString(data);
    }

    public static byte[] decodeBase64(String base64EncodedString) {
        return base64Decoder.decode(base64EncodedString);
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

    public static String logString(UUID streamID, String operation, long operationNanoTime, int chunkCount) {
        StringBuffer sb = new StringBuffer();
        sb.append(LocalDateTime.now() + "\t");
        sb.append(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory() + "\t");
        sb.append(streamID.toString() + "\t");
        sb.append(operation + "\t");
        sb.append(String.format(Locale.ROOT, "%.2f", (float) (operationNanoTime)/1000000) + "\t");
        sb.append(chunkCount + "\t");
        return sb.toString();
    }
}
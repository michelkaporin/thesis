package treedb.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.n1analytics.paillier.PaillierPublicKey;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public class Utility {
    public static List<byte[]> base64EncodedStringsToByteArrays(String str) {
        List<byte[]> result = new ArrayList<byte[]>();
        if (str.equals("[]")) return result;
        
        Decoder base64Decoder = Base64.getDecoder();
        String[] byteArrays = str.substring(1, str.length() - 1).split(","); // remove square braces and split by ','
        for (String encodedData : byteArrays) {
            result.add(base64Decoder.decode(encodedData.substring(1, encodedData.length() - 1))); // remove quotes
        }

        return result;
    }

    public static String marshalPaillierPublicKey(PaillierPublicKey pubKey) {
        return pubKey.getModulus().toString();
    }

    // Taken from https://stackoverflow.com/a/9855338/1713082
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        
        return new String(hexChars);
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
}
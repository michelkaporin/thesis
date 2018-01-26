package treedb.client.utils;

import com.n1analytics.paillier.PaillierPublicKey;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static List<byte[]> byteArrayStringToByteArrays(String str) {
        List<byte[]> result = new ArrayList<byte[]>();
        if (str.equals("[]")) return result;
        
        String[] byteArrays = str.substring(1, str.length() - 1).split(",(?=\\[)");
        for (String array : byteArrays) {
            String[] byteValues = array.substring(1, array.length() - 1).split(",");
            byte[] bytes = new byte[byteValues.length];
            for (int i = 0, len = bytes.length; i < len; i++) {
                bytes[i] = Byte.parseByte(byteValues[i].trim());
            }
            result.add(bytes);
        }

        return result;
    }

    public static String marshalPaillierPublicKey(PaillierPublicKey pubKey) {
        return pubKey.getModulus().toString();
    }
}
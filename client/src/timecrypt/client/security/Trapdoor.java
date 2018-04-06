package timecrypt.client.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.BitSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import timecrypt.client.utils.Utility;

public class Trapdoor {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private byte[] key = new byte[16];

    public Trapdoor() throws NoSuchAlgorithmException {
        // Generate symmetric key for the trapdoor
        SecureRandom random = new SecureRandom();
        random.nextBytes(this.key);
    }

    public Trapdoor(byte[] key) {
        this.key = key;
    }

    public byte[] getKey() {
        return this.key;
    }

    /**
     * Based on https://eprint.iacr.org/2003/216.pdf
     */
    public String getFilter(String tag, double falsePositiveProbability, int expectedNumberOfElements) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        BloomFilter<String> bf = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
        bf.add(this.getTrapdoor(tag, bf.getK()));

        return Arrays.toString(bf.getBitSet().toLongArray());
    }

    public boolean containsTag(String tag, BitSet bs, double falsePositiveProbability, int expectedNumberOfElements) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        BloomFilter<String> bf = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
        bf.setBitSet(bs);
        
        return bf.contains(this.getTrapdoor(tag, bf.getK()));
    }

    private String getTrapdoor(String word, int bfHashFunctionsCount) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        byte[] wordBytes = word.getBytes();

        ByteArrayOutputStream trapdoor = new ByteArrayOutputStream();
        byte[] key = this.key;
        for (int i=0; i < bfHashFunctionsCount; i++) {
            trapdoor.write(this.getSignature(wordBytes, key));
            key = getSignature(this.key, key);
        }

        return Utility.bytesToHex(trapdoor.toByteArray());
    }

    private byte[] getSignature(byte[] key, byte[] value) throws InvalidKeyException, NoSuchAlgorithmException {
        SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(keySpec);
        
        return mac.doFinal(value);
    }
}
package treedb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ch.ethz.inf.dsg.crypto.OPE;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import treedb.client.TreeDB;
import treedb.client.security.CryptoKeyPair;
import treedb.client.security.Trapdoor;
import treedb.client.utils.Utility;

public class UnitTest {

    private final static String IP = "127.0.0.1";
    private final static int PORT = 8001;

    private static CryptoKeyPair keys;

    private static Trapdoor td;
    private final static double BF_FALSEPOSITIVE_PROBABILITY = 0.01;
    private final static int BF_EXPECTED_NUM_OF_TAGS = 16;

    private static byte[] opeKey;
    private static OPE ope;
    
    private TreeDB client;

    @BeforeClass
    public static void setupClass() throws IOException, NoSuchAlgorithmException {
        keys = CryptoKeyPair.generateKeyPair();
        td = new Trapdoor();
        opeKey = new byte[16];
        ope = new OPE(opeKey, 64, 128);
    }

    @Before
    public void setupClient() throws IOException {
        this.client = new TreeDB(IP, PORT);
        this.client.openConnection();
    }
    
    @After
    public void teardownClient() throws IOException {
        this.client.closeConnection();
    }

    @Test
    public void createStream() throws IOException {
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true, 'count': true, 'tags': true }", keys.publicKey);
        assertNotNull(streamID);
    }

    @Test
    public void insert() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true, 'count': true, 'tags': true }", keys.publicKey);
        assertNotNull(streamID);

        for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			BigInteger sum = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
            BigInteger count = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
            
            String keyAndData = String.format("%s-%s", from, to); // assume encrypted
            
            String tags = td.getFilter("test" + keyAndData, BF_FALSEPOSITIVE_PROBABILITY, BF_EXPECTED_NUM_OF_TAGS);
            
			BigInteger min = ope.encrypt(BigInteger.valueOf(from));
			BigInteger max = ope.encrypt(BigInteger.valueOf(to));

            boolean res = client.insert(streamID, keyAndData, keyAndData.getBytes(), getMetadataJson(from, to, sum, count, min, max, tags));
            assertEquals(true, res);
        }
    }

    @Test
    public void getStatistics() throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        // Create stream
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true, 'count': true, 'tags': true }", keys.publicKey);
        assertNotNull(streamID);

        // Perform insert
        for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			BigInteger sum = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
            BigInteger count = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
            String keyAndData = String.format("%s-%s", from, to);
            String tags = td.getFilter("test" + keyAndData, BF_FALSEPOSITIVE_PROBABILITY, BF_EXPECTED_NUM_OF_TAGS);
			BigInteger min = ope.encrypt(BigInteger.valueOf(from));
			BigInteger max = ope.encrypt(BigInteger.valueOf(to));
            boolean res = client.insert(streamID, keyAndData, keyAndData.getBytes(), getMetadataJson(from, to, sum, count, min, max, tags));
            assertEquals(true, res);
        }

        // Retrieve statistics
        long from = 7;
		long to = 12;
        String metadataResult = client.getStatistics(streamID, from, to);
        
		JsonParser parser = new JsonParser();
		JsonObject jObj = parser.parse(metadataResult).getAsJsonObject();
		BigInteger sum = jObj.get("sum").getAsBigInteger();
		BigInteger count = jObj.get("count").getAsBigInteger();
		BigInteger min = jObj.get("min").getAsBigInteger();
        BigInteger max = jObj.get("max").getAsBigInteger();
        JsonArray tags = jObj.get("tags").getAsJsonArray();
        
        assertEquals(BigInteger.valueOf(3), keys.privateKey.raw_decrypt(sum));
        assertEquals(BigInteger.valueOf(3), keys.privateKey.raw_decrypt(count));
        assertEquals(BigInteger.valueOf(7), ope.decrypt(min));
        assertEquals(BigInteger.valueOf(12), ope.decrypt(max));
        
        BitSet bs = Utility.unmarshalBitSet(tags);
        assertEquals(true, td.containsTag("test7-8", bs, BF_FALSEPOSITIVE_PROBABILITY, BF_EXPECTED_NUM_OF_TAGS));
        assertEquals(true, td.containsTag("test11-12", bs, BF_FALSEPOSITIVE_PROBABILITY, BF_EXPECTED_NUM_OF_TAGS));
        assertEquals(false, td.containsTag("test8-9", bs, BF_FALSEPOSITIVE_PROBABILITY, BF_EXPECTED_NUM_OF_TAGS));
    }

    @Test
    public void getRange() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        // Create stream
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true, 'count': true, 'tags': true }", keys.publicKey);
        assertNotNull(streamID);

        // Perform insert
        for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			BigInteger sum = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
            BigInteger count = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
            String keyAndData = String.format("%s-%s", from, to);
            String tags = td.getFilter("test" + keyAndData, BF_FALSEPOSITIVE_PROBABILITY, BF_EXPECTED_NUM_OF_TAGS);
			BigInteger min = ope.encrypt(BigInteger.valueOf(from));
			BigInteger max = ope.encrypt(BigInteger.valueOf(to));
            boolean res = client.insert(streamID, keyAndData, keyAndData.getBytes(), getMetadataJson(from, to, sum, count, min, max, tags));
            assertEquals(true, res);
        }

        // Retrieve a range
		long from = 2;
		long to = 7;

		System.out.format("\nRetrieving %s..%s\n", from, to);
        List<byte[]> retrievedRange = client.getRange(streamID, from, to);
        
        int i = 1;
        for (byte[] data : retrievedRange) {
            assertEquals(String.format("%s-%s", i, i+1), new String(data));
            i += 2;
        }
    }

    private String getMetadataJson(long from, long to, BigInteger sum, BigInteger count, BigInteger min, BigInteger max, String tags) {
        return String.format("{ 'from': %s, 'to': %s, 'sum': %s, 'count': %s, 'min': %s, 'max': %s, 'tags': %s }", from, to, sum, count, min, max, tags);
    }
}
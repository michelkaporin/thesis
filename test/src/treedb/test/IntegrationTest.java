package treedb.test;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;

import treedb.client.TreeDB;
import treedb.client.security.CryptoKeyPair;
import treedb.client.security.OPEWrapper;
import treedb.client.security.Trapdoor;
import treedb.client.utils.Utility;
import treedb.server.Server;

public class IntegrationTest {
    public static void main(String[] args) throws Exception {
        String ip = args[0];
		int port = Integer.valueOf(args[1]);
		
		//testEncryption();
		testFunctions(ip, port);
		//testMultipleClients(ip, port);
	}
	
	// private static void testEncryption() {
	// 	CryptoKeyPair keys = CryptoKeyPair.generateKeyPair();
	// 	BigInteger sum1 = keys.publicKey.raw_encrypt(new BigInteger(String.valueOf(1)));
	// 	BigInteger sum2 = keys.publicKey.raw_encrypt(new BigInteger(String.valueOf(1)));

    //     PaillierContext context = keys.publicKey.createSignedContext();
	// 	EncryptedNumber number1 = new EncryptedNumber(context, sum1, 2048);
	// 	EncryptedNumber number2 = new EncryptedNumber(context, sum2, 2048);
	// 	EncryptedNumber sum = number1.add(number2);
	// 	BigInteger bigSum = sum.calculateCiphertext();

	// 	System.out.println(keys.privateKey.raw_decrypt(bigSum));
	// 	System.out.println(new BigInteger("1").add(new BigInteger("1")));
	// }

	private static void testFunctions(String ip, int port) throws Exception {
        // Server server = new Server(ip, port);
		// new Thread(server).start();

		TreeDB client = new TreeDB(ip, port);
		client.openConnection();
		
		byte[] key = new byte[16];
		CryptoKeyPair keys = CryptoKeyPair.generateKeyPair();
		Trapdoor td = new Trapdoor();
		OPEWrapper ope = new OPEWrapper();
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true, 'count': true, 'tags': true }", keys.publicKey, null);
		testInsert(client, streamID, keys.publicKey, td, ope);
        testGetRange(client, streamID);
		testGetStatistics(client, streamID, keys.privateKey, td, ope);

		client.closeConnection();
		// server.terminate();
	}

	private static void testInsert(TreeDB client, String streamID, PaillierPublicKey pubKey, Trapdoor td, OPEWrapper ope) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			BigInteger sum = pubKey.raw_encrypt(new BigInteger(String.valueOf(1)));
			BigInteger count = pubKey.raw_encrypt(new BigInteger(String.valueOf(1)));
			String keyAndData = String.format("%s-%s", from, to); // data should be encrypted in a non-paillier way
			String tags = td.getFilter("test" + keyAndData, 0.01, 16);
			BigInteger min = ope.encrypt(BigInteger.valueOf(from));
			BigInteger max = ope.encrypt(BigInteger.valueOf(to));

			String md = String.format("{ 'from': %s, 'to': %s, 'sum': %s, 'count': %s, 'min': %s, 'max': %s, 'tags': %s }", from, to, sum, count, min, max, tags);
			System.out.println(md);
			client.insert(streamID, keyAndData, keyAndData.getBytes(), md);
			System.out.format("Adding from %s to %s\n", from, to);
        }
	}
	
	private static void testGetStatistics(TreeDB client, String streamID, PaillierPrivateKey privKey, Trapdoor td, OPEWrapper ope) throws Exception {
		long from = 7;
		long to = 12;
		System.out.format("Querying for stats in %s..%s\n", from, to);
		String metadataResult = client.getStatistics(streamID, from, to);
		
		System.out.println(metadataResult);

		JsonParser parser = new JsonParser();
		JsonObject jObj = parser.parse(metadataResult).getAsJsonObject();
		BigInteger sum = jObj.get("sum").getAsBigInteger();
		BigInteger count = jObj.get("count").getAsBigInteger();
		BigInteger min = jObj.get("min").getAsBigInteger();
		BigInteger max = jObj.get("max").getAsBigInteger();
		System.out.format("sum: %s; count: %s\n", privKey.raw_decrypt(sum), privKey.raw_decrypt(count));
		System.out.format("min: %s; max: %s\n", ope.decrypt(min), ope.decrypt(max));

		JsonArray tags = jObj.get("tags").getAsJsonArray();
		BitSet bs = Utility.unmarshalBitSet(tags);
		boolean containsTag1 = td.containsTag("test7-8", bs, 0.01, 16);
		boolean containsTag2 = td.containsTag("test11-12", bs, 0.01, 16);
		boolean containsTag3 = td.containsTag("test8-9", bs, 0.01, 16);
		System.out.println(String.format("Contains tags: %s %s %s", containsTag1, containsTag2, containsTag3));
	}

	private static List<byte[]> testGetRange(TreeDB client, String streamID) throws IOException {
		long from = 2;
		long to = 7;

		System.out.format("\nRetrieving %s..%s\n", from, to);
		List<byte[]> retrievedRange = client.getRange(streamID, from, to);
		for (byte[] r : retrievedRange) {
			System.out.println(new String(r));
		}

		return retrievedRange;
	}

	private static void testMultipleClients(String ip, int port) throws IOException, InterruptedException {
        Server server = new Server(ip, port, null);
		new Thread(server).start();

		ExecutorService exec = Executors.newCachedThreadPool();
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

		for (int i=0; i < 10; i++) {
			Callable<Void> c = new Callable<>() {
				@Override
				public Void call() throws Exception {
					TreeDB client = new TreeDB(ip, port);
					client.openConnection();
		
					CryptoKeyPair keys = CryptoKeyPair.generateKeyPair();
					Trapdoor td = new Trapdoor();
					OPEWrapper ope = new OPEWrapper();
					//String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true }");
					//testInsert(client, streamID);
					//testGetRange(client, streamID);
					testGetStatistics(client, "streamID", keys.privateKey, td, ope);

					return null;
				}		
			};
			tasks.add(c);
		}

		exec.invokeAll(tasks);
		server.terminate();
	}
}
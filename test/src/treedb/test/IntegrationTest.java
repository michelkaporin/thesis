package treedb.test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;

import treedb.client.TreeDB;
import treedb.client.utils.CryptoKeyPair;
import treedb.server.Server;

public class IntegrationTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String ip = args[0];
		int port = Integer.valueOf(args[1]);
		
		//testEncryption();
		testBasicFunctions(ip, port);
		//testMultipleClients(ip, port);
	}
	
	// private static void testEncryption() {
	// 	CryptoKeyPair keys = CryptoKeyPair.generateKeyPair();
	// 	BigInteger sum1 = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
	// 	BigInteger sum2 = keys.publicKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));

    //     PaillierContext context = keys.publicKey.createSignedContext();
	// 	EncryptedNumber number1 = new EncryptedNumber(context, sum1, 2048);
	// 	EncryptedNumber number2 = new EncryptedNumber(context, sum2, 2048);
	// 	EncryptedNumber sum = number1.add(number2);
	// 	BigInteger bigSum = sum.calculateCiphertext();

	// 	System.out.println(keys.privateKey.raw_decrypt(bigSum));
	// 	System.out.println(new BigInteger("1").add(new BigInteger("1")));
	// }

	private static void testBasicFunctions(String ip, int port) throws IOException {
        Server server = new Server(ip, port);
		new Thread(server).start();

		TreeDB client = new TreeDB(ip, port);
		client.openConnection();
		
		CryptoKeyPair keys = CryptoKeyPair.generateKeyPair();
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true, 'count': true }", keys.publicKey);
		testInsert(client, streamID, keys.publicKey);
        testGetRange(client, streamID);
		testGetStatistics(client, streamID, keys.privateKey);

		client.closeConnection();
		server.terminate();
	}

	private static void testInsert(TreeDB client, String streamID, PaillierPublicKey pubKey) throws IOException {
		for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			BigInteger sum = pubKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
			BigInteger count = pubKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
			String keyAndData = String.format("%s-%s", from, to); // data should be encrypted in a non-paillier way

			String md = String.format("{ 'from': %s, 'to': %s, 'sum': %s, 'count': %s, 'min': %s, 'max': %s}", from, to, sum, count, from, to);
			System.out.println(md);
			client.insert(streamID, keyAndData, keyAndData.getBytes(), md);
			System.out.format("Adding from %s to %s\n", from, to);
        }
	}
	
	private static void testGetStatistics(TreeDB client, String streamID, PaillierPrivateKey privKey) throws IOException {
		long from = 7;
		long to = 12;
		System.out.format("Querying for stats in %s..%s\n", from, to);
		String metadataResult = client.getStatistics(streamID, from, to);
		
		System.out.println(metadataResult);

		JsonParser parser = new JsonParser();
		JsonObject jObj = parser.parse(metadataResult).getAsJsonObject();
		BigInteger sum = jObj.get("sum").getAsBigInteger();
		BigInteger count = jObj.get("count").getAsBigInteger();
		System.out.format("sum: %s; count: %s\n", privKey.raw_decrypt(sum), privKey.raw_decrypt(count));
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
        Server server = new Server(ip, port);
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
					//String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true }");
					//testInsert(client, streamID);
					//testGetRange(client, streamID);
					testGetStatistics(client, "streamID", keys.privateKey);

					return null;
				}		
			};
			tasks.add(c);
		}

		exec.invokeAll(tasks);
		server.terminate();
	}
}
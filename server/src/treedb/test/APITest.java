package treedb.test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;

import treedb.server.API;

public class APITest {

	public static void main(String[] args) throws IOException {
		PaillierPrivateKey pKey = PaillierPrivateKey.create(2048);
		PaillierPublicKey pubKey = pKey.getPublicKey();
		
		// Create and populate stream
		String jsonContract = "{ 'count': true, 'sum': true, 'min': false, 'max': false }";
		String streamID = (String) API.createStream(2, jsonContract, pubKey, null);
		
		for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			String keyAndData = String.format("%s-%s", from, to);

			BigInteger sum = pubKey.raw_encrypt_without_obfuscation(new BigInteger(String.valueOf(1)));
			BigInteger count = sum;

			String md = String.format("{ 'from': %s, 'to': %s, 'sum': %s, 'count': %s }", from, to, sum, count);
			System.out.println(md);
			Object res = API.insert(UUID.fromString(streamID), keyAndData, keyAndData.getBytes(), md);
			if (res instanceof Boolean) {
				System.out.format("Adding from %s to %s\n", from, to);
			} else {
				throw new IOException("Failed to add " + from + "-" + to);
			}
		}

		testGetRange(streamID);
		testGetStatistics(streamID, pKey);
	}

	private static void testGetStatistics(String streamID, PaillierPrivateKey privKey) {
		long from = 7;
		long to = 12;
		System.out.format("Querying for stats in %s..%s\n", from, to);
		String metadataResult = (String) API.getStatistics(UUID.fromString(streamID), from, to);

		JsonParser parser = new JsonParser();
		JsonObject jObj = parser.parse(metadataResult).getAsJsonObject();
		BigInteger sum = jObj.get("sum").getAsBigInteger();
		BigInteger count = jObj.get("count").getAsBigInteger();
		System.out.format("sum: %s; count: %s\n", privKey.raw_decrypt(sum), privKey.raw_decrypt(count));

	}

	private static List<byte[]> testGetRange(String streamID) {
		long from = 2;
		long to = 7;

		System.out.format("\nRetrieving %s..%s\n", from, to);
		List<byte[]> retrievedRange = (List<byte[]>) API.getRange(UUID.fromString(streamID), from, to);
		for (byte[] r : retrievedRange) {
			System.out.println(new String(r));
		}

		return retrievedRange;
	}
}

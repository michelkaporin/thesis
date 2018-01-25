package treedb.test;

import java.util.List;
import java.util.UUID;
import treedb.server.API;

public class APITest {

	public static void main(String[] args) {
		// Create and populate stream
		String jsonContract = "{ 'count': true, 'sum': true, 'min': true, 'max': true }";
		String streamID = (String) API.createStream(2, jsonContract);
		for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			String keyAndData = String.format("%s-%s", from, to);

			String md = String.format("{ 'from': %s, 'to': %s, 'sum': 1, 'count': 1, 'min': %s, 'max': %s}", from, to, from, to);
			System.out.println(md);
			API.insert(UUID.fromString(streamID), keyAndData, keyAndData.getBytes(), md);
			System.out.format("Adding from %s to %s\n", from, to);
		}

		testGetRange(streamID);
		testGetStatistics(streamID);
	}

	private static void testGetStatistics(String streamID) {
		long from = 7;
		long to = 12;
		System.out.format("Querying for stats in %s..%s\n", from, to);
		System.out.println(API.getStatistics(UUID.fromString(streamID), from, to));
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

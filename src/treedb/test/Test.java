package treedb.test;

import java.util.List;
import java.util.UUID;
import treedb.server.API;
import treedb.server.Metadata;

public class Test {

	public static void main(String[] args) {
		// Insert chunk
		UUID streamID = API.createStream(2);
		for (int i = 1; i < 10; i += 2) {
			long from = i;
			long to = i+1;
			String keyAndData = String.format("%s-%s", from, to);
			Metadata md = new Metadata(from, to, 0, 0, 0, 0);

			API.insert(streamID, keyAndData, keyAndData.getBytes(), md);
			System.out.format("Adding from %s to %s\n", from, to);
		}

		int from = 2;
		int to = 7;
		System.out.format("\nRetrieving %s..%s\n", from, to);
		List<byte[]> retrievedRange = API.getRange(streamID, from, to);
		for (byte[] r : retrievedRange) {
			System.out.println(new String(r));
		}
	}

}

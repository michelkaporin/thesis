package treedb.test;

import java.util.List;
import java.util.UUID;
import treedb.server.API;

public class Test {

	public static void main(String[] args) {
		// Insert chunk
		UUID streamID = API.createStream(2);
		for (int i = 1; i < 10; i += 2) {
			long fromTime = i;
			long toTime = i+1;
			String keyAndData = String.format("%s-%s", fromTime, toTime);
			
			API.insert(streamID, keyAndData, keyAndData.getBytes(), fromTime, toTime);
			System.out.format("Adding from %s to %s\n", fromTime, toTime);
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

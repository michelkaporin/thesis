package treedb;

import java.util.List;
import java.util.UUID;
import treedb.server.API;

public class Main {

	public static void main(String[] args) {
		// Insert chunk
		UUID streamID = API.createStream(2);
		for (int i = 1; i < 10; i += 2) {
			long fromTime = i;
			long toTime = i+1;
			byte[] data = String.format("%s-%s", fromTime, toTime).getBytes();
			API.insert(streamID, data, fromTime, toTime);
			System.out.format("Adding from %s to %s\n", fromTime, toTime);
		}

		System.out.println("\nRetrieving 2..7");
		List<byte[]> retrievedRange = API.getRange(streamID, 8, 10);
		for (byte[] r : retrievedRange) {
			System.out.println(new String(r));
		}
	}

}

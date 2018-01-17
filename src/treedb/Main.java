package treedb;

import java.util.UUID;
import treedb.server.API;

public class Main {

	public static void main(String[] args) {
		// Insert chunk
		UUID streamID = API.createStream(2);
		for (int i = 0; i < 7; i += 2) {
			byte[] data = "1-2".getBytes();
			long fromTime = i;
			long toTime = i+1;
			API.insert(streamID, data, fromTime, toTime);
			System.out.format("Adding from %s to %s\n", fromTime, toTime);
		}

		//API.getRange(streamID, 1, 7);
	}

}

package treedb.test;

import java.io.IOException;
import java.util.List;
import treedb.client.TreeDB;
import treedb.server.Server;

public class CommunicationTest {
    public static void main(String[] args) throws IOException {
        String ip = args[0];
        int port = Integer.valueOf(args[1]);
        Server server = new Server(ip, port);
        new Thread(server).start();
        
        TreeDB client = new TreeDB(ip, port);
        client.openConnection();
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true }");

		for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			String keyAndData = String.format("%s-%s", from, to);

			String md = String.format("{ 'from': %s, 'to': %s, 'sum': 1, 'count': 1, 'min': %s, 'max': %s}", from, to, from, to);
			System.out.println(md);
			client.insert(streamID, keyAndData, keyAndData.getBytes(), md);
			System.out.format("Adding from %s to %s\n", from, to);
        }

        testGetRange(client, streamID);
		testGetStatistics(client, streamID);
    }
    
	private static void testGetStatistics(TreeDB client, String streamID) throws IOException {
		long from = 7;
		long to = 12;
		System.out.format("Querying for stats in %s..%s\n", from, to);
		String metadataResult = client.getStatistics(streamID, from, to);
		System.out.println(metadataResult);
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
}
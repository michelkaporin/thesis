package treedb.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import treedb.client.TreeDB;
import treedb.server.Server;

public class CommunicationTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String ip = args[0];
		int port = Integer.valueOf(args[1]);
		
		testBasicFunctions(ip, port);
		testMultipleClients(ip, port);
	}
	
	private static void testBasicFunctions(String ip, int port) throws IOException {
        Server server = new Server(ip, port);
		new Thread(server).start();

		TreeDB client = new TreeDB(ip, port);
		client.openConnection();
		
        String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true }");
		testInsert(client, streamID);
        testGetRange(client, streamID);
		testGetStatistics(client, streamID);

		client.closeConnection();
		server.terminate();
	}

	private static void testInsert(TreeDB client, String streamID) throws IOException {
		for (int i = 1; i < 16; i += 2) {
			long from = i;
			long to = i+1;
			String keyAndData = String.format("%s-%s", from, to);

			String md = String.format("{ 'from': %s, 'to': %s, 'sum': 1, 'count': 1, 'min': %s, 'max': %s}", from, to, from, to);
			System.out.println(md);
			client.insert(streamID, keyAndData, keyAndData.getBytes(), md);
			System.out.format("Adding from %s to %s\n", from, to);
        }
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
		
					//String streamID = client.createStream(2, "{ 'sum': true, 'min': true, 'max': true }");
					//testInsert(client, streamID);
					//testGetRange(client, streamID);
					testGetStatistics(client, "streamID");

					return null;
				}		
			};
			tasks.add(c);
		}

		exec.invokeAll(tasks);
		server.terminate();
	}
}
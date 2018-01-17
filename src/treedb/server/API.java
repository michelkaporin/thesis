package treedb.server;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import treedb.server.tree.Chunk;
import treedb.server.tree.Tree;

public class API {
	
	private static Map<UUID, Tree> treeMap = new HashMap<UUID, Tree>();

	public static UUID createStream(int k) {
		UUID id = UUID.randomUUID();
		treeMap.put(id, new Tree(k));
		return id;
	}

	public static void insert(UUID streamID, byte[] data, long fromTime, long toTime) {
		Tree tree = treeMap.get(streamID);
		if (tree == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		Chunk chunk = new Chunk(data);
		tree.insert(chunk, fromTime, toTime);
	}

	public static List<byte[]> getRange(UUID streamID, long fromTime, long toTime) {
		Tree tree = treeMap.get(streamID);
		if (tree == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		return tree.getRange(fromTime, toTime);
	}
	
	public static void getStatistics(Date from, Date to) {
		throw new UnsupportedOperationException();
	}
}

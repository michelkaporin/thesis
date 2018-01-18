package treedb.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import treedb.server.tree.Tree;
import treedb.server.tree.storage.FileSystem;
import treedb.server.tree.storage.Storage;

public class API {
	
	private static Map<UUID, Tree> treeMap = new HashMap<UUID, Tree>();
	private static Storage storage;

	public static UUID createStream(int k) {
		UUID id = UUID.randomUUID();
		storage = new FileSystem();

		treeMap.put(id, new Tree(k, storage));
		return id;
	}

	public static void insert(UUID streamID, String key, byte[] data, long fromTime, long toTime) {
		Tree tree = treeMap.get(streamID);
		if (tree == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		if (storage.store(streamID.toString(), key, data)) {
			tree.insert(key, fromTime, toTime);
		} else {
			throw new RuntimeException("Insertion failed to happen.");
		}
	}

	public static List<byte[]> getRange(UUID streamID, long fromTime, long toTime) {
		Tree tree = treeMap.get(streamID);
		if (tree == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		List<byte[]> results = new ArrayList<byte[]>();
		for (String key : tree.getRange(fromTime, toTime)) {
			results.add(storage.get(streamID.toString(), key));
		}

		return results;
	}
	
	public static void getStatistics(Date from, Date to) {
		throw new UnsupportedOperationException();
	}
}

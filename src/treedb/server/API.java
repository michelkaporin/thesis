package treedb.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import treedb.server.index.Tree;
import treedb.server.storage.FileSystem;
import treedb.server.storage.Storage;

public class API {
	
	private static Map<UUID, Tree> indexMap = new HashMap<UUID, Tree>();
	private static Storage storage;

	public static UUID createStream(int k) {
		UUID id = UUID.randomUUID();
		storage = new FileSystem();

		indexMap.put(id, new Tree(k));
		return id;
	}

	public static void insert(UUID streamID, String key, byte[] data, Metadata metadata) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		if (storage.store(streamID.toString(), key, data)) {
			index.insert(key, metadata);
		} else {
			throw new RuntimeException("Insertion failed to happen.");
		}
	}

	public static List<byte[]> getRange(UUID streamID, long fromTime, long toTime) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		List<byte[]> results = new ArrayList<byte[]>();
		for (String key : index.getRange(fromTime, toTime)) {
			results.add(storage.get(streamID.toString(), key));
		}

		return results;
	}
	
	public static Metadata getStatistics(UUID streamID, long from, long to) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			throw new NoSuchElementException("No stream exists for the following ID.");
		}

		List<Metadata> metadata = index.getMetadata(from, to);
		
		return Metadata.consolidate(metadata);
	}
}

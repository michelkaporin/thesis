package treedb.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import treedb.server.index.Metadata;
import treedb.server.index.MetadataConfiguration;
import treedb.server.index.Tree;
import treedb.server.storage.FileSystem;
import treedb.server.storage.Storage;
import treedb.server.utils.FailureJson;

public class API {
	
	private static Map<UUID, Tree> indexMap = new HashMap<UUID, Tree>();
	private static Storage storage;
	private static Gson gson = new Gson();

	public static Object createStream(int k, String json) {
		UUID id = UUID.randomUUID();
		storage = new FileSystem();

		MetadataConfiguration mc = null;
		try {
			mc = gson.fromJson(json, MetadataConfiguration.class);
		} catch (JsonSyntaxException e) {
			return new FailureJson("JSON provided for metadata is incorrect.");
		}

		indexMap.put(id, new Tree(k, mc));
		return id.toString();
	}

	public static Object insert(UUID streamID, String key, byte[] data, String metadata) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			return new FailureJson("No stream exists for the following ID.");
		}
		
		Metadata md = null;
		try {
			md = gson.fromJson(metadata, Metadata.class);
		} catch (JsonSyntaxException e) {
			return new FailureJson("JSON provided for metadata is incorrect.");
		}
		
		// Check config match
		if (!md.matchesConfig(index.getMetadataConfig())) {
			return new FailureJson("Metadata provided does not match metadata configuration for this stream.");
		}

		if (storage.store(streamID.toString(), key, data)) {
			index.insert(key, md);
		} else {
			return new FailureJson("Insertion failed to happen due to storage problems.");
		}

		return true;
	}

	public static Object getRange(UUID streamID, long fromTime, long toTime) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			return new FailureJson("No stream exists for the following ID.");
		}

		List<byte[]> results = new ArrayList<byte[]>();
		for (String key : index.getRange(fromTime, toTime)) {
			results.add(storage.get(streamID.toString(), key));
		}

		return results;
	}
	
	public static Object getStatistics(UUID streamID, long from, long to) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			return new FailureJson("No stream exists for the following ID.");
		}

		List<Metadata> metadata = index.getMetadata(from, to);
		return Metadata.consolidate(index.getMetadataConfig(), metadata).toJson(index.getMetadataConfig());
	}
}

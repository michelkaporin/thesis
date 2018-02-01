package treedb.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.n1analytics.paillier.PaillierPublicKey;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
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
import treedb.server.utils.Utility;

public class API {
	
	private static Map<UUID, Tree> indexMap = new HashMap<UUID, Tree>();
	private static Storage storage;
	private static Gson gson = new Gson();
    private static JsonParser jsonParser = new JsonParser();

	public static Object createStream(int k, String json, PaillierPublicKey pubKey) {
		UUID id = UUID.randomUUID();
		storage = new FileSystem();

		MetadataConfiguration mc = null;
		try {
			mc = gson.fromJson(json, MetadataConfiguration.class);
		} catch (JsonSyntaxException e) {
			return new FailureJson("JSON provided for metadata is incorrect.");
		}
		mc.setPaillierPublicKey(pubKey);

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
			JsonObject jobject = jsonParser.parse(metadata).getAsJsonObject();
			long from = jobject.get("from").getAsLong();
			long to = jobject.get("to").getAsLong();

			BigInteger sum = null, count = null, min = null, max = null;
			BitSet tags = null;
			JsonElement jSum = jobject.get("sum");
			JsonElement jCount = jobject.get("count");
			JsonElement jMin = jobject.get("min");
			JsonElement jMax = jobject.get("max");
			JsonElement jTags = jobject.get("tags");
			if (jSum != null) sum = jSum.getAsBigInteger();
			if (jCount != null) count = jSum.getAsBigInteger();
			if (jMin != null) min = jMin.getAsBigInteger();
			if (jMax != null) max = jMax.getAsBigInteger();
			if (jTags != null) tags = Utility.unmarshalBitSet(jTags.getAsJsonArray());
			md = new Metadata(index.getMetadataConfig(), from, to, sum, count, min, max, tags);
		} catch (JsonSyntaxException e) {
			return new FailureJson("JSON provided for metadata is incorrect.");
		}
		
		// Check config match
		if (!md.matchesConfig(index.getMetadataConfig())) {
			return new FailureJson("Metadata provided does not match metadata configuration for this stream.");
		}

		if (!index.dataIsNewer(md)) {
			return new FailureJson("The index is append-only. Insertion of data in the middle is not allowed.");
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

		List<String> results = new ArrayList<String>();
		for (String key : index.getRange(fromTime, toTime)) {
			byte[] retrieved = storage.get(streamID.toString(), key);
			results.add(Utility.encodeBase64(retrieved));
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

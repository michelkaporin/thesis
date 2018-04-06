package timecrypt.server;

import ch.ethz.dsg.ecelgamal.ECElGamal.ECElGamalCiphertext;
import ch.ethz.dsg.ore.ORE.ORECiphertext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierPublicKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import timecrypt.server.index.Metadata;
import timecrypt.server.index.MetadataConfiguration;
import timecrypt.server.index.enums.HomomorphicAlgorithm;
import timecrypt.server.index.enums.OrderPreservingAlgorithm;
import timecrypt.server.index.Tree;
import timecrypt.server.index.crypto.HomomorphicEncryptedNumber;
import timecrypt.server.index.crypto.OrderPreservingEncryptedNumber;
import timecrypt.server.storage.FileSystem;
import timecrypt.server.storage.S3;
import timecrypt.server.storage.Storage;
import timecrypt.server.utils.FailureJson;
import timecrypt.server.utils.Utility;

public class API {
    private static Logger LOGGER_PERFORMANCE = Logger.getLogger("TimeCrypt Performance");

	private static final int PAILLIER_EXPONENT = 2048;
	
	private static Map<UUID, Tree> indexMap = new HashMap<UUID, Tree>();
	private static Storage storage;
	private static Gson gson = new Gson();
	private static JsonParser jsonParser = new JsonParser();
	private static String[] arguments;

	public static void init(String[] args)
    {
        arguments = args;
    }

	public static Object createStream(int k, String metaConfig, PaillierPublicKey pubKey, String datalayer) {
		long start = System.nanoTime();

		UUID id = null;
		// Ensure uniqueness of stream identifier within the system
		while (id == null || indexMap.containsKey(id)) { 
			id = UUID.randomUUID();
		}

		switch (datalayer) {
			case "s3":
				storage = new S3(id.toString(), arguments);
				break;
			default:
				storage = new FileSystem();
				break;
		}

		MetadataConfiguration mc = null;
		try {
			mc = gson.fromJson(metaConfig, MetadataConfiguration.class);
		} catch (JsonSyntaxException e) {
			return new FailureJson("JSON provided for metadata is incorrect.");
		}
		mc.setPaillierPublicKey(pubKey);

		indexMap.put(id, new Tree(k, mc));
		LOGGER_PERFORMANCE.info(Utility.logString(id, "createStream", System.nanoTime()-start, 0));
		
		return id.toString();
	}

	public static Object deleteStream(UUID streamID) {
		indexMap.remove(streamID);
		System.gc();
		return true;
	}

	public static Object insert(UUID streamID, String key, byte[] data, String metadata) {
		long start = System.nanoTime();

		Tree index = indexMap.get(streamID);
		if (index == null) {
			return new FailureJson("No stream exists for the following ID.");
		}
		
		MetadataConfiguration mdConfig = index.getMetadataConfig();
		Metadata md = null;
		try {
			JsonObject jobject = jsonParser.parse(metadata).getAsJsonObject();
			long from = jobject.get("from").getAsLong();
			long to = jobject.get("to").getAsLong();

			HomomorphicEncryptedNumber sum = null, count = null;
			OrderPreservingEncryptedNumber min = null, max = null;
			BigInteger first = null, last = null;
			BitSet tags = null;
			JsonElement jSum = jobject.get("sum");
			JsonElement jCount = jobject.get("count");
			JsonElement jMin = jobject.get("min");
			JsonElement jMax = jobject.get("max");
			JsonElement jFirst = jobject.get("first");
			JsonElement jLast = jobject.get("last");
			JsonElement jTags = jobject.get("tags");

			if (jSum != null && mdConfig.sum) {
				try {
					sum = new HomomorphicEncryptedNumber(new EncryptedNumber(mdConfig.getPaillierContext(), jSum.getAsBigInteger(), PAILLIER_EXPONENT));
				} catch (NumberFormatException e) { // ecelgamal case
					if (mdConfig.algorithms.sum == HomomorphicAlgorithm.ECELGAMAL) {
						sum = new HomomorphicEncryptedNumber(ECElGamalCiphertext.decode(Base64.getDecoder().decode(jSum.getAsString())));
					} else {
						throw e;
					}
				}
			}
			if (jCount != null && mdConfig.count) {
				try {
					count = new HomomorphicEncryptedNumber(new EncryptedNumber(mdConfig.getPaillierContext(), jSum.getAsBigInteger(), PAILLIER_EXPONENT));
				} catch (NumberFormatException e) {
					if (mdConfig.algorithms.count == HomomorphicAlgorithm.ECELGAMAL) {
						count = new HomomorphicEncryptedNumber(ECElGamalCiphertext.decode(Base64.getDecoder().decode(jCount.getAsString())));
					} else {
						throw e;
					}
				}
			}
			if (jMin != null && mdConfig.min) { 
				try {
					min = new OrderPreservingEncryptedNumber(jMin.getAsBigInteger());
				} catch (NumberFormatException e) {
					if (mdConfig.algorithms.min == OrderPreservingAlgorithm.ORE) {
						min = new OrderPreservingEncryptedNumber(ORECiphertext.decodeDefault(Base64.getDecoder().decode(jMin.getAsString())));
					} else {
						throw e;
					}
				}
			}
			if (jMax != null && mdConfig.max) {
				try {
					max = new OrderPreservingEncryptedNumber(jMax.getAsBigInteger());
				} catch (NumberFormatException e) {
					if (mdConfig.algorithms.max == OrderPreservingAlgorithm.ORE) {
						max = new OrderPreservingEncryptedNumber(ORECiphertext.decodeDefault(Base64.getDecoder().decode(jMax.getAsString())));
					} else {
						throw e;
					}
				}
			}
			if (jFirst != null && mdConfig.first) first = jFirst.getAsBigInteger();
			if (jLast != null && mdConfig.last) last = jLast.getAsBigInteger();
			if (jTags != null && mdConfig.tags) tags = Utility.unmarshalBitSet(jTags.getAsJsonArray());
			md = new Metadata(from, to, sum, count, min, max, first, last, tags);
		} catch (Exception e) {
			return new FailureJson("JSON provided for metadata is incorrect.");
		}
		
		// Check config match
		if (!md.matchesConfig(mdConfig)) {
			return new FailureJson("Metadata provided does not match metadata configuration for this stream.");
		}

		if (!index.dataIsNewer(md)) {
			return new FailureJson("The index is append-only. Insertion of data in the middle is not allowed.");
		}

		index.insert(key, md);
		LOGGER_PERFORMANCE.info(Utility.logString(streamID, "insert", System.nanoTime()-start, index.getLeavesCount()));

		if (!storage.store(streamID.toString(), key, data)) {
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
			byte[] retrieved = null;
			try {
				retrieved = storage.get(streamID.toString(), key);
				results.add(Utility.encodeBase64(retrieved));
			} catch (Exception e) {
				return new FailureJson("Failed to retrieve the results due to: " + e.toString());
			}
		}

		if (results.size() == 0) {
			return new FailureJson("There is no data stored under the range requested.");
		}
		
		return results;
	}
	
	public static Object getStatistics(UUID streamID, long from, long to) {
		Tree index = indexMap.get(streamID);
		if (index == null) {
			return new FailureJson("No stream exists for the following ID.");
		}

		List<Metadata> metadata = index.getMetadata(from, to);
		if (metadata.size() == 0) {
			return new FailureJson("There is no data information about the range provided.");
		}

		return Metadata.consolidate(index.getMetadataConfig(), metadata).toJson(index.getMetadataConfig());
	}
}

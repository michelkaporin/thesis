package treedb.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.n1analytics.paillier.PaillierPublicKey;
import treedb.client.json.CreateStreamRequest;
import treedb.client.json.DeleteStreamRequest;
import treedb.client.json.GetRangeRequest;
import treedb.client.json.GetStatisticsRequest;
import treedb.client.json.InsertRequest;
import treedb.client.utils.Utility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TreeDB {

    private static Logger LOGGER = Logger.getLogger("TreeDB Client");

    private String ip;
    private int port;
    private SocketChannel channel;

    private JsonParser jsonParser;
    private Gson gson;
    
    public TreeDB(String ip, int port) {
        this.ip = ip;
        this.port = port;

        gson = new Gson();
        jsonParser = new JsonParser();
        LOGGER.setLevel(Level.SEVERE);
    }

    public void openConnection() throws IOException {
        InetSocketAddress hostAddress = new InetSocketAddress(this.ip, this.port);
        try {
			this.channel = SocketChannel.open(hostAddress);
		} catch (IOException e) {
            LOGGER.severe("Failed to open a socket to the server");
			throw e;
		}
    }

    public void closeConnection() throws IOException {
        try {
			this.channel.close();
		} catch (IOException e) {
            LOGGER.severe("Failed to close the connection to the server.");
            throw e;
		}
    }

    public String createStream(int k, String contract, PaillierPublicKey pubKey, String storage) throws IOException {
        String publicKey = null;
        if (pubKey != null) publicKey = Utility.marshalPaillierPublicKey(pubKey);
        String json = gson.toJson(new CreateStreamRequest(k, contract, publicKey, storage));
        LOGGER.info(json);
        String result = getResult(json);
        return gson.fromJson(result, String.class);
    }
    
    public boolean insert(String streamID, String key, byte[] data, String metadata) throws IOException {
        String json = gson.toJson(new InsertRequest(streamID, key, Base64.getEncoder().encodeToString(data), metadata));
        LOGGER.info(json);

        return Boolean.valueOf(getResult(json));
    }

    public boolean delete(String streamID) throws IOException {
        String json = gson.toJson(new DeleteStreamRequest(streamID));
        LOGGER.info(json);

        return Boolean.valueOf(getResult(json));
    }

    public List<byte[]> getRange(String streamID, long fromTime, long toTime) throws IOException {
        String json = gson.toJson(new GetRangeRequest(streamID, fromTime, toTime));
        LOGGER.info(json);
        String result = getResult(json);
        
        return Utility.base64EncodedStringsToByteArrays(result);
    }

    public String getStatistics(String streamID, long fromTime, long toTime) throws IOException {
        String json = gson.toJson(new GetStatisticsRequest(streamID, fromTime, toTime));
        LOGGER.info(json);

        return gson.fromJson(getResult(json), String.class);
    }

    private String getResult(String requestJson) throws IOException {
        String apiResult = new String(writeAndRead(requestJson));

        try {
            JsonObject jobject = jsonParser.parse(apiResult).getAsJsonObject();
            String exception = jobject.get("msg").getAsString();
            LOGGER.info("Failed to perform operation: " + exception);
            throw new IOException(exception);
        } catch (NullPointerException | JsonParseException | IllegalStateException e) {
            return apiResult;
        }
    }

	private byte[] writeAndRead(String json) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());
        int bytesWritten = 0;
        try {
            while (bytesWritten != buffer.capacity()) {
                bytesWritten += this.channel.write(buffer);
            }
		} catch (IOException e) {
            LOGGER.severe("Failed to send the command to the server.");
            throw e;
        }

        buffer = ByteBuffer.allocate(5120);  // 5 MB buffer max
        int numRead = 0;
        try {
            numRead = channel.read(buffer);
        } catch (IOException e) {
            LOGGER.severe("Failed to read the result of the command.");
            throw e;
        }
        if (numRead == -1) {
            LOGGER.severe("Failed to read the result of the command.");
            throw new IOException("Failed to read the result of the command.");
        }

        byte[] trimmedBytes = new byte[numRead];
        System.arraycopy(buffer.array(), 0, trimmedBytes, 0, numRead);
        
        return trimmedBytes;
    }
}
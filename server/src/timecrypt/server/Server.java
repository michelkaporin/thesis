package timecrypt.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.n1analytics.paillier.PaillierPublicKey;

import timecrypt.server.utils.FailureJson;
import timecrypt.server.utils.Utility;

public class Server implements Runnable {
    private static Logger LOGGER_API = Logger.getLogger("TimeCrypt API");

    private ServerSocketChannel serverChannel;
    private Selector selector; 
    private Gson gson;
    private JsonParser jsonParser;
    
    private Map<SocketChannel, byte[]> incompleteRequests;
    private Map<SocketChannel, Long> incompleteRequestsHistory;
    
    public Server(String ip, int port, String[] args) throws IOException {
        initChannel(ip, port);
        gson = new Gson();
        jsonParser = new JsonParser();
        incompleteRequests = new HashMap<SocketChannel, byte[]>();
        incompleteRequestsHistory = new HashMap<SocketChannel, Long>();
        LOGGER_API.setLevel(Level.WARNING);
        API.init(args);
    }
    
    public void run() {
        LOGGER_API.info("Server is running");
        while (serverChannel.isOpen()) {
            try {
                selector.select();
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();

                    if (key.isAcceptable()) {
                        // Accept incoming connection
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        // deserialise the message and call the API
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(102400);
                        int numRead = client.read(buffer);

                        if (numRead == -1) {
                            client.close();
                            continue;
                        }

                        // Call the API and return the result, put it back to the readable state
                        byte[] trimmedBytes = new byte[numRead];
                        System.arraycopy(buffer.array(), 0, trimmedBytes, 0, numRead);
                        
                        byte[] request = trimmedBytes;
                        boolean retryParsing = true;
                        int trials = 0;
                        while (retryParsing) {
                            try {
                                Object apiResult = null;
                                try {
                                    apiResult = callMethod(new String(request, Charset.forName("UTF-8")));
                                } catch (OutOfMemoryError e) { // Out of memory error should not fail the server
                                    apiResult = new FailureJson("TimeCrypt ran out of memory.");
                                }

                                // write back the result to channel
                                String response = gson.toJson(apiResult);
                                client.write(ByteBuffer.wrap(response.getBytes()));

                                retryParsing = false;
                                incompleteRequests.remove(client);
                                client.register(selector, SelectionKey.OP_READ);
                            } catch (JsonSyntaxException e) {
                                Long trial = incompleteRequestsHistory.get(client);
                                if (trial != null && trial+300000L < System.currentTimeMillis()) { // allow only 5 minutes to get the full request
                                    client.close();
                                    incompleteRequestsHistory.remove(client);
                                    break;
                                }
                                if (trials > 0) {
                                    incompleteRequests.put(client, request);
                                    break;
                                }
                                
                                byte[] partialRequest = incompleteRequests.get(client);
                                if (partialRequest == null) {
                                    incompleteRequests.put(client, trimmedBytes);
                                    incompleteRequestsHistory.put(client, System.currentTimeMillis());
                                    retryParsing = false;
                                } else {
                                    request = new byte[partialRequest.length + trimmedBytes.length];
                                    System.arraycopy(partialRequest, 0, request, 0, partialRequest.length);
                                    System.arraycopy(trimmedBytes, 0, request, partialRequest.length, trimmedBytes.length);
                                }
                                trials++;
                            }
                        }
                    }

                    selectedKeys.remove();
                }
			} catch (IOException | ClosedSelectorException e) {
				LOGGER_API.severe(e.toString() + ": " + e.getMessage());
			}
        }
    }

    public void terminate() throws IOException {
        serverChannel.close();
        serverChannel.keyFor(selector).cancel();
        selector.close();
    }

	private void initChannel(String ip, int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(ip, port));
		serverChannel.register(selector, serverChannel.validOps());
    }

    private Object callMethod(String json) {
        LOGGER_API.info(json);
        JsonObject jobject = null;
        try {
            jobject = jsonParser.parse(json).getAsJsonObject();
        } catch (IllegalStateException e) {
            return new FailureJson("JSON provided is incorrect.");
        }
        
        try {
            String operationName = jobject.get("operationID").getAsString();
            switch (operationName) {
                case "insert": {
                    String streamID = jobject.get("streamID").getAsString();
                    String key = jobject.get("key").getAsString();
                    String data = jobject.get("data").getAsString();
                    String metadata = jobject.get("metadata").getAsString();

                    return API.insert(Utility.UUIDFromString(streamID), key, Utility.decodeBase64(data), metadata);
                }
                case "create": {
                    int k = jobject.get("k").getAsInt();
                    String contract = jobject.get("contract").getAsString();
                    
                    PaillierPublicKey pubkey = null;                
                    try {
                        String pubKeyModulus = jobject.get("modulus").getAsString();
                        pubkey = Utility.unmarshalPublicKey(pubKeyModulus);
                    } catch (Exception e) {}
                    
                    String storage;
                    try { 
                        storage = jobject.get("storage").getAsString();
                    } catch (Exception e) {
                        storage = "";
                    }
                    return API.createStream(k, contract, pubkey, storage.toLowerCase());
                }
                case "getrange": {
                    String streamID = jobject.get("streamID").getAsString();
                    long from = jobject.get("from").getAsLong();
                    long to = jobject.get("to").getAsLong();

                    return API.getRange(Utility.UUIDFromString(streamID), from, to);
                }
                case "getstatistics": {
                    String streamID = jobject.get("streamID").getAsString();
                    long from = jobject.get("from").getAsLong();
                    long to = jobject.get("to").getAsLong();

                    return API.getStatistics(Utility.UUIDFromString(streamID), from, to);
                }
                case "delete": {
                    String streamID = jobject.get("streamID").getAsString();
                    return API.deleteStream(Utility.UUIDFromString(streamID));
                }
                default: {
                    String msg = String.format("Operation %s is not supported", operationName);
                    LOGGER_API.warning(msg);
                    return new FailureJson(msg);
                }
            }
        } catch (NullPointerException e) {
            return new FailureJson("One of the command required JSON attributes were not provided."); 
        }
    }
}
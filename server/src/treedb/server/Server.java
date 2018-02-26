package treedb.server;

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
import treedb.server.utils.Utility;

public class Server implements Runnable {
    private static Logger LOGGER = Logger.getLogger("TreeDB Server");

    private ServerSocketChannel serverChannel;
    private Selector selector; 
    private Gson gson;
    private JsonParser jsonParser;
    
    private Map<SocketChannel, byte[]> incompleteRequests;
    
    public Server(String ip, int port, String[] args) throws IOException {
        initChannel(ip, port);
        gson = new Gson();
        jsonParser = new JsonParser();
        incompleteRequests = new HashMap<SocketChannel, byte[]>();
        LOGGER.setLevel(Level.SEVERE);
        API.init(args);
    }
    
    public void run() {
        LOGGER.info("Server is running");
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

                        // Call the API and return the result, put it back to the readable state
                        byte[] trimmedBytes = new byte[numRead];
                        System.arraycopy(buffer.array(), 0, trimmedBytes, 0, numRead);
                        
                        byte[] request = trimmedBytes;
                        boolean retryParsing = true;
                        int trials = 0;
                        while (retryParsing) {
                            try {
                                Object apiResult = callMethod(new String(request, Charset.forName("UTF-8")));
                                if (apiResult != null) {
                                    // write back the result to channel
                                    String response = gson.toJson(apiResult);
                                    client.write(ByteBuffer.wrap(response.getBytes()));
                                }

                                retryParsing = false;
                                incompleteRequests.remove(client);
                                client.register(selector, SelectionKey.OP_READ);
                            } catch (JsonSyntaxException e) {
                                if (trials > 0) {
                                    incompleteRequests.put(client, request);
                                    break;
                                }
                                
                                byte[] partialRequest = incompleteRequests.get(client);
                                if (partialRequest == null) {
                                    incompleteRequests.put(client, trimmedBytes);
                                    retryParsing = false;
                                } else {
                                    request = new byte[partialRequest.length + trimmedBytes.length];
                                    System.arraycopy(partialRequest, 0, request, 0, partialRequest.length);
                                    System.arraycopy(trimmedBytes, 0, request, partialRequest.length+1, trimmedBytes.length);
                                }
                                trials++;                                
                            }
                        }
                    }

                    selectedKeys.remove();
                }
			} catch (IOException | ClosedSelectorException e) {
				LOGGER.severe(e.toString() + ": " + e.getMessage());
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
        LOGGER.info(json);
        JsonObject jobject = jsonParser.parse(json).getAsJsonObject();
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
            default: {
                LOGGER.warning(String.format("Operation %s is not supported", operationName));
            }
        }

        return null;
    }
}
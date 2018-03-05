package timecrypt.baseline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import timecrypt.server.utils.FailureJson;

public class BaselineServer implements Runnable {
    private ServerSocketChannel serverChannel;
    private Selector selector; 
    private MetadataList list;
    private JsonParser jsonParser;
    private Gson gson;
    
    public BaselineServer(String ip, int port) throws IOException {
        initChannel(ip, port);
        list = new MetadataList();
        jsonParser = new JsonParser();
        gson = new GsonBuilder().disableHtmlEscaping().create();
    }
    
    public void run() {
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
                        
                        Object apiResult = callMethod(new String(trimmedBytes));
                        String response = gson.toJson(apiResult);
                        client.write(ByteBuffer.wrap(response.getBytes()));
                    }

                    selectedKeys.remove();
                }
			} catch (IOException | ClosedSelectorException e) {
				System.out.println(e.toString() + ": " + e.getMessage());
			}
        }
    }

    public Object callMethod(String json) {
        JsonObject jobject = null;
        try {
            jobject = jsonParser.parse(json).getAsJsonObject();
        } catch (IllegalStateException e) {
            return new FailureJson("JSON provided is incorrect.");
        }
    
        String operationName = jobject.get("operationID").getAsString();
        if (operationName.equals("insert")) {
            long from = jobject.get("from").getAsLong();
            long to = jobject.get("to").getAsLong();
            String data = jobject.get("data").getAsString();
            return list.add(new Metadata(from, to, Base64.getDecoder().decode(data)));
        } else if (operationName.equals("getRange")) {
            long from = jobject.get("from").getAsLong();
            long to = jobject.get("to").getAsLong();
            return list.getEncodedRange(from, to);
        } else if (operationName.equals("cleanList")) {
            return list.clean();
        } else {
            return "Operation not supported";
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
}
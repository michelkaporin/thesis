package treedb.server.storage;

import java.io.IOException;

public interface Storage {
    boolean store(String streamID, String key, byte[] data);
    byte[] get(String streamID, String key) throws IOException;
}
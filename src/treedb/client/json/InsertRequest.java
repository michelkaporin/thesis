package treedb.client.json;

public class InsertRequest {
    private final String operationID = "insert";

    private String streamID;
    private String key;
    private byte[] data;
    private String metadata;

    public InsertRequest(String streamID, String key, byte[] data, String metadata) {
        this.streamID = streamID;
        this.key = key;
        this.data = data;
        this.metadata = metadata;
    }
}
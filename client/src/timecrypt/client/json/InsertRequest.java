package timecrypt.client.json;

public class InsertRequest {
    private final String operationID = "insert";

    private String streamID;
    private String key;
    private String data;
    private String metadata;

    public InsertRequest(String streamID, String key, String data, String metadata) {
        this.streamID = streamID;
        this.key = key;
        this.data = data;
        this.metadata = metadata;
    }
}
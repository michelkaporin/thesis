package treedb.client.json;

public class DeleteStreamRequest {
    private final String operationID = "delete";

    private String streamID;

    public DeleteStreamRequest(String streamID) {
        this.streamID = streamID;
    }
}
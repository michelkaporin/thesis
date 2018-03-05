package timecrypt.client.json;

public class GetRangeRequest {
    private final String operationID = "getrange";

    private String streamID;
    private long from;
    private long to;

    public GetRangeRequest(String streamID, long from, long to) {
        this.streamID = streamID;
        this.from = from;
        this.to = to;
    }
}
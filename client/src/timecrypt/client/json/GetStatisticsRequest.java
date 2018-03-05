package timecrypt.client.json;

public class GetStatisticsRequest {
    private final String operationID = "getstatistics";

    private String streamID;
    private long from;
    private long to;

    public GetStatisticsRequest(String streamID, long from, long to) {
        this.streamID = streamID;
        this.from = from;
        this.to = to;
    }
}
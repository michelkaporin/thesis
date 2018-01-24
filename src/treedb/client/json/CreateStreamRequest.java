package treedb.client.json;

public class CreateStreamRequest {
    private final String operationID = "create";

    private int k;
    private String contract;

    public CreateStreamRequest(int k, String contract) {
        this.k = k;
        this.contract = contract;
    }
}
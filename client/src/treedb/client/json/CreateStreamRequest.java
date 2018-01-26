package treedb.client.json;

public class CreateStreamRequest {
    private final String operationID = "create";

    private int k;
    private String contract;
    private String modulus;

    public CreateStreamRequest(int k, String contract, String publicKeyModulus) {
        this.k = k;
        this.contract = contract;
        this.modulus = publicKeyModulus;
    }
}
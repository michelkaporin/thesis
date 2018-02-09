package treedb.client.json;

public class CreateStreamRequest {
    private final String operationID = "create";

    private int k;
    private String contract;
    private String modulus;
    private String storage;

    public CreateStreamRequest(int k, String contract, String publicKeyModulus, String storage) {
        this.k = k;
        this.contract = contract;
        this.modulus = publicKeyModulus;
        this.storage = storage;
    }
}
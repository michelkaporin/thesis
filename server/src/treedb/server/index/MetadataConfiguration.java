package treedb.server.index;

import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPublicKey;

public class MetadataConfiguration {
    public boolean count;
    public boolean sum;
    public boolean min;
    public boolean max;

    private PaillierPublicKey publicKey;
    private PaillierContext paillierContext;

    public void setPaillierPublicKey(PaillierPublicKey publicKey) {
        this.publicKey = publicKey;
        this.paillierContext = publicKey.createSignedContext();
    }

    public PaillierContext getPaillierContext() {
        return this.paillierContext;
    }
}
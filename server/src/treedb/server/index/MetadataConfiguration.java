package treedb.server.index;

import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPublicKey;

public class MetadataConfiguration {
    public boolean count;
    public boolean sum;
    public boolean min;
    public boolean max;
    public boolean first;
    public boolean last;
    public boolean tags;

    private PaillierContext paillierContext;

    public void setPaillierPublicKey(PaillierPublicKey publicKey) {
        this.paillierContext = publicKey.createSignedContext();
    }

    public PaillierContext getPaillierContext() {
        return this.paillierContext;
    }
}
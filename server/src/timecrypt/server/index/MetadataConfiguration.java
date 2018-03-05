package timecrypt.server.index;

import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPublicKey;

import timecrypt.server.index.enums.HomomorphicAlgorithm;
import timecrypt.server.index.enums.OrderPreservingAlgorithm;

public class MetadataConfiguration {
    public final boolean count;
    public final boolean sum;
    public final boolean min;
    public final boolean max;
    public final boolean first;
    public final boolean last;
    public final boolean tags;
    public final CryptoAlgorithmsConfiguration algorithms;

    private PaillierContext paillierContext;

    public MetadataConfiguration(boolean count, boolean sum, boolean min, boolean max, boolean first, boolean last, boolean tags, CryptoAlgorithmsConfiguration algorithms) {
        this.count = count;
        this.sum = sum;
        this.min = min;
        this.max = max;
        this.first = first;
        this.last = last;
        this.tags = tags;
        this.algorithms = algorithms;
    }

    public void setPaillierPublicKey(PaillierPublicKey publicKey) {
        if (publicKey != null) {
            this.paillierContext = publicKey.createSignedContext();
        }
    }

    public PaillierContext getPaillierContext() {
        return this.paillierContext;
    }

    public class CryptoAlgorithmsConfiguration {
        public final HomomorphicAlgorithm sum;
        public final HomomorphicAlgorithm count;
        public final OrderPreservingAlgorithm min;
        public final OrderPreservingAlgorithm max;

        public CryptoAlgorithmsConfiguration(OrderPreservingAlgorithm min, OrderPreservingAlgorithm max, HomomorphicAlgorithm sum, HomomorphicAlgorithm count) {
            this.min = min;
            this.max = max;
            this.sum = sum;
            this.count = count;
        }
    }
}
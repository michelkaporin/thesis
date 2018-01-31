package treedb.server.index;

import com.n1analytics.paillier.EncryptedNumber;

import treedb.server.utils.Utility;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Metadata {
    private static final int EXPONENT = 2048;

    public long from;
    public long to;

	public EncryptedNumber sum;
	public EncryptedNumber count;
	public EncryptedNumber min;
    public EncryptedNumber max;
    public BitSet tags;
    
    public Metadata() {
        this.min = null;
        this.max = null;
        this.sum = null;
        this.count = null;
        this.tags = null;
    }

    public Metadata(MetadataConfiguration config, long from, long to, BigInteger sum, BigInteger count, BigInteger min, BigInteger max, BitSet tags) {
        this.from = from;
        this.to = to;
        
        if (sum != null) {
            this.sum = new EncryptedNumber(config.getPaillierContext(), sum, EXPONENT);
        }
        if (count != null) {
            this.count = new EncryptedNumber(config.getPaillierContext(), count, EXPONENT);
        }
        if (min != null) {
            this.min = new EncryptedNumber(config.getPaillierContext(), min, EXPONENT);
        }
        if (max != null) {
            this.max = new EncryptedNumber(config.getPaillierContext(), max, EXPONENT);
        }
        if (tags != null) {
            this.tags = tags;
        }
    }

    public Metadata(long from, long to, EncryptedNumber sum, EncryptedNumber count, EncryptedNumber min, EncryptedNumber max, BitSet tags) {
        this.from = from;
        this.to = to;
        
        this.sum = sum;
        this.count = count;
        this.min = min;
        this.max = max;
        this.tags = tags;
    }

    public boolean matchesConfig(MetadataConfiguration config) {
        if ((config.count && count == null) 
            || (config.max && max == null)
            || (config.min && min == null)
            || (config.sum && sum == null)
            || (config.tags && tags == null)) {
            return false;
        }

        return true;
    }

    public static void updateMetadata(MetadataConfiguration config, Metadata updateFrom, Metadata updateTo) {
        if (updateTo.from == 0L) {
            updateTo.from = updateFrom.from;
        }
        updateTo.to = updateFrom.to;

        if (config.count) {
            updateTo.count = updateTo.count == null ? updateFrom.count : updateTo.count.add(updateFrom.count);
        }
        if (config.sum) {
            updateTo.sum = updateTo.sum == null ? updateFrom.sum : updateTo.sum.add(updateFrom.sum);
        }
        if (config.tags) {
            if (updateTo.tags == null) {
                updateTo.tags = updateFrom.tags;
            } else {
                // Merge bitsets
                Utility.mergeBitSet(updateFrom.tags, updateTo.tags);
            }
        }
        // TODO: Min/Max
    }

    public static Metadata consolidate(MetadataConfiguration config, List<Metadata> metadata) {
        long from = Long.MAX_VALUE, to = Long.MIN_VALUE;
        EncryptedNumber sum = null, count = null, min = null, max = null;
        BitSet bs = new BitSet();

        for (Metadata md : metadata) {
            if (md.from < from) {
                from = md.from;
            }
            if (md.to > to) {
                to = md.to;
            }

            if (config.sum) {
                sum = sum == null ? md.sum : sum.add(md.sum);
            }
            if (config.count) {
                count = count == null ? md.count : count.add(md.count);
            }
            if (config.tags) {
                Utility.mergeBitSet(md.tags, bs);
            }
            // TODO: Min/Max
        }

        return new Metadata(from, to, sum, count, min, max, bs);
    }

    public String toJson(MetadataConfiguration config) {
        StringBuilder str = new StringBuilder();
        str.append("{");
        str.append(String.format("\"from\": %s, \"to\": %s", from, to));
        if (config.sum) str.append(", \"sum\": " + sum.calculateCiphertext());
        if (config.min && min != null) str.append(", \"min\": " + min.calculateCiphertext()); // != null safety check while min/max implementation in progress
        if (config.max && min != null) str.append(", \"max\": " + max.calculateCiphertext());
        if (config.count) str.append(", \"count\": " + count.calculateCiphertext());
        if (config.tags) str.append(", \"tags\": " + Arrays.toString(tags.toLongArray()));
        str.append("}");

        return str.toString();
    }
}
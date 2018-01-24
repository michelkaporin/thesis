package treedb.server;

import java.util.List;

public class Metadata {
    public long from;
    public long to;

	public long sum;
	public int count;
	public long min;
    public long max;
    
    public Metadata() {
        this.min = Long.MAX_VALUE;
        this.max = Long.MIN_VALUE;
        this.sum = 0;
        this.count = 0;
    }

    public Metadata(long from, long to, long sum, int count, long min, long max) {
        this.from = from;
        this.to = to;
        
        this.sum = sum;
        this.count = count;
        this.min = min;
        this.max = max;
    }

    public boolean matchesConfig(MetadataConfiguration config) {
        if ((config.count && count == 0) 
            || (config.max && max == Long.MIN_VALUE)
            || (config.min && min == Long.MAX_VALUE)
            || (config.sum && sum == 0)) {
            return false;
        }

        return true;
    }

    public static void updateMetadata(MetadataConfiguration config, Metadata updateFrom, Metadata updateTo) {
        if (updateTo.from == 0L) {
            updateTo.from = updateFrom.from;
        }
        updateTo.to = updateFrom.to;

        if (config.count) updateTo.count += updateFrom.count;
        if (config.max) updateTo.max = Math.max(updateTo.max, updateFrom.max);
        if (config.min) updateTo.min = Math.min(updateTo.min, updateFrom.min);
        if (config.sum) updateTo.sum += updateFrom.sum;
    }

    public static Metadata consolidate(MetadataConfiguration config, List<Metadata> metadata) {
        long from = Long.MAX_VALUE, 
            to = Long.MIN_VALUE,
            sum = 0L, 
            min = Long.MAX_VALUE, 
            max = Long.MIN_VALUE;
        int count = 0;

        for (Metadata md : metadata) {
            if (md.from < from) {
                from = md.from;
            }
            if (md.to > to) {
                to = md.to;
            }

            if (config.sum) sum += md.sum;
            if (config.min) min = Math.min(md.min, min); 
            if (config.max) max = Math.max(md.max, max);
            if (config.count) count += md.count;
        }

        return new Metadata(from, to, sum, count, min, max);
    }

    public String toString() {
        return String.format("From\tTo\tSum\tMin\tMax\tCount\n%s\t%s\t%s\t%s\t%s\t%s", from, to, sum, min, max, count);
    }

    public String toJson(MetadataConfiguration config) {
        StringBuilder str = new StringBuilder();
        str.append("{");
        str.append(String.format("\"from\": %s, \"to\": %s", from, to));
        if (config.sum) str.append(", \"sum\": " + sum);
        if (config.min) str.append(", \"min\": " + min);
        if (config.max) str.append(", \"max\": " + max);
        if (config.count) str.append(", \"count\": " + count);
        str.append("}");

        return str.toString();
    }
}
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

    public static Metadata consolidate(List<Metadata> metadata) {
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
            sum += md.sum;
            min = Math.min(md.min, min);
            max = Math.max(md.max, max);
            count += md.count;
        }

        return new Metadata(from, to, sum, count, min, max);
    }

    public String toString() {
        return String.format("From\tTo\tSum\tMin\tMax\tCount\n%s\t%s\t%s\t%s\t%s\t%s", from, to, sum, min, max, count);
    }
}
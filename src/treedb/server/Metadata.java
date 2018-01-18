package treedb.server;

public class Metadata {
    public long from;
    public long to;

	public int sum;
	public int count;
	public int min;
    public int max;
    
    public Metadata() {
    }

    public Metadata(long from, long to, int sum, int count, int min, int max) {
        this.from = from;
        this.to = to;
        
        this.sum = sum;
        this.count = count;
        this.min = min;
        this.max = max;
    }
}
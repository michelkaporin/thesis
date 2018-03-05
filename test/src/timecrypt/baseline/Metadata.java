package timecrypt.baseline;

public class Metadata {
    public long from;
    public long to;
    public byte[] data;

    public Metadata(long from, long to, byte[] data) {
        this.from = from;
        this.to = to;
        this.data = data;
    }
}
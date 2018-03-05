package timecrypt.baseline;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MetadataList {
    private ArrayList<Metadata> metadataList;

    public MetadataList() {
        metadataList = new ArrayList<>();
    }

    public boolean add(Metadata data) {
        return metadataList.add(data);
    }

    public List<String> getEncodedRange(long from, long to) {
        List<String> l = new ArrayList<String>();

        for (Metadata m : metadataList) {
            if (m.from >= from && to <= m.to) {
                l.add(Base64.getEncoder().encodeToString(m.data));
            }
        }

        return l;
    }

    public boolean clean() {
        this.metadataList = new ArrayList<>();
        return true;
    }
}
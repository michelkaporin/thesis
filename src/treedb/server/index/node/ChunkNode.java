package treedb.server.index.node;

import treedb.server.Metadata;

public class ChunkNode extends Node {
    public String storeKey;
    
    public ChunkNode(Metadata metadata, String storeKey) {
        super(metadata);
        this.storeKey = storeKey;
    }
}
package treedb.server.index.node;

import treedb.server.index.Metadata;

public class ChunkNode extends Node {
    public String storeKey;
    
    public ChunkNode(Metadata metadata, String storeKey) {
        super(metadata);
        this.storeKey = storeKey;
    }
}
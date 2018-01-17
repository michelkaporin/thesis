package treedb.server.tree;

public class Chunk {
	MetaData metadata;
	byte[] data;
	
	public Chunk(byte[] data, MetaData metadata) {
		this.data = data; // should go on persistent storage, instead of having it in memory as byte[]
		this.metadata = metadata;
	}
}

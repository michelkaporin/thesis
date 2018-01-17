package treedb.server.tree;

public class Chunk {
	byte[] data;
	
	public Chunk(byte[] data) {
		this.data = data; // should go on persistent storage, instead of having it in memory as byte[]
	}
}

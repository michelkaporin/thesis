package treedb.server;

import java.util.Date;

import treedb.server.tree.Chunk;
import treedb.server.tree.MetaData;
import treedb.server.tree.Tree;

public class API {
	
	private static Tree<Chunk> tree;
	
	public static void Insert(byte[] data, MetaData metadata) {
		Chunk chunk = new Chunk(data, metadata);
		
		if (tree == null) {
			tree = new Tree<Chunk>(2);
		}
		
		tree.insert(chunk);
	}

	public static void GetRange(Date from, Date to) {
		throw new UnsupportedOperationException();
	}
	
	public static void GetStatistics(Date from, Date to) {
		throw new UnsupportedOperationException();
	}
}

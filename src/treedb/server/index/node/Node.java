package treedb.server.index.node;

import java.util.ArrayList;
import java.util.List;
import treedb.server.Metadata;

public class Node {
	public Node parent;
	public List<Node> children;

	public Metadata metadata;
	
	public Node() {
		this(new Metadata());
	}
	
	public Node(Metadata metadata) {
		this.children = new ArrayList<Node>();
		this.metadata = metadata;
	}
}

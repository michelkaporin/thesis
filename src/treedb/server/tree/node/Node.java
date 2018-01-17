package treedb.server.tree.node;

import java.util.ArrayList;
import java.util.List;

public class Node {
	public Node parent;
	public List<Node> children;

    public long from;
    public long to;
	
	public Node() {
		this.children = new ArrayList<Node>();
	}
}

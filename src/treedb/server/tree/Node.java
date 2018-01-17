package treedb.server.tree;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
	T chunk;
	
	public Node<T> parent;
	public List<Node<T>> children;
	
	public Node() {
		this.children = new ArrayList<Node<T>>();
	}
}

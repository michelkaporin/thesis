package treedb.server.tree;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
	private Node<T> root;
	private int k; // maximum amount of children per node
	
	private List<Node<T>> lastNodes; // Stores last node under each tree level, level 0 reserved to the leaf nodes
	
	public Tree(int k) {
		root = new Node<T>();
		
		this.k = k;
		lastNodes = new ArrayList<Node<T>>();
		lastNodes.add(root);
	}
	
	/**
	 * Inserts chunk into the leaf node, constructing k-ary tree in a bottom-up way
	 */
	public void insert(T chunk) {
		Node<T> insertNode = new Node<T>();
		insertNode.chunk = chunk;
		
		int currentLevel = 0;
		
		// Check if parent node has available edges to store the node, if not
		Node<T> lastLevelNode = lastNodes.get(currentLevel);

		if (lastLevelNode.children.size() < k) {
			lastLevelNode.children.add(insertNode);
			this.updateStats()
			return;
		}

		Node<T> previousNode = insertNode;
		while (lastLevelNode != null && lastLevelNode.children.size() >= k) {
			Node<T> newLevelNode = new Node<T>();
			newLevelNode.children.add(previousNode);
			lastNodes.set(currentLevel, newLevelNode); // update last node on the current level
			previousNode = newLevelNode; // Update reference to the node that needs to be connected later on			

			// Navigate to the upper level in the tree
			try {
				lastLevelNode = lastNodes.get(++currentLevel);
			} catch (IndexOutOfBoundsException e) {
				lastLevelNode = null;
			}
		}

		// The tree has to grow in one level up
		if (lastLevelNode == null) {
			Node<T> lastRoot = root;
			root = new Node<T>();
			root.children.add(lastRoot); // point last root to the new root
			root.children.add(previousNode); // point last created node to the new root
		}
	}

	private void updateMetadata(Node<T> existingNode, Node<T> newNode) {
		node.chunk.
	}
}

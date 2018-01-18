package treedb.server.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import treedb.server.tree.node.ChunkNode;
import treedb.server.tree.node.MetaNode;
import treedb.server.tree.node.Node;
import treedb.server.tree.storage.Storage;

/**
 * TODO: Consider the cases when the tree is in the middle of insert operation and other data is inserted/read from it
 */
public class Tree {
	private MetaNode root;
	private int k; // maximum amount of children per node
	private Storage storage;
	
	private List<MetaNode> lastNodes; // Stores last node under each tree level, level 0 reserved to the leaf nodes
	
	public Tree(int k, Storage s) {
		root = new MetaNode();
		
		this.k = k;
		this.storage = s;
		
		lastNodes = new ArrayList<MetaNode>();
		lastNodes.add(root);
	}
	
	/**
	 * Inserts chunk into the leaf node, constructing k-ary tree in a bottom-up way
	 */
	public void insert(String key, long from, long to) {
		ChunkNode insertNode = new ChunkNode();
		insertNode.from = from;
		insertNode.to = to;
		insertNode.storeKey = key;
		
		int currentLevel = 0;
		
		// Check if parent node has available edges to store the node, if not
		MetaNode lastLevelNode = lastNodes.get(currentLevel);

		if (lastLevelNode.children.size() < k) {
			lastLevelNode.children.add(insertNode);
			insertNode.parent = lastLevelNode;
			this.updateMetadata(insertNode);
			return;
		}

		Node previousNode = insertNode;
		while (lastLevelNode != null && lastLevelNode.children.size() >= k) {
			MetaNode newLevelNode = new MetaNode();
			newLevelNode.children.add(previousNode);
			previousNode.parent = newLevelNode;
			lastNodes.set(currentLevel, newLevelNode); // update last node on the current level
			this.updateMetadata(previousNode); // update metadata for newly created node
			previousNode = newLevelNode; // Update reference to the node that needs to be connected later on			

			// Navigate to the upper level in the tree
			currentLevel += 1;
			try {
				lastLevelNode = lastNodes.get(currentLevel);
			} catch (IndexOutOfBoundsException e) {
				lastLevelNode = null;
			}
		}

		// The tree has to grow in one level up
		if (lastLevelNode == null) {
			MetaNode lastRoot = root;
			root = new MetaNode();
			root.children.add(lastRoot); // point last root to the new root
			root.children.add(previousNode); // point last created node to the new root
			lastRoot.parent = root;
			previousNode.parent = root;
			lastNodes.add(root);
			this.updateMetadata(lastRoot);
			this.updateMetadata(previousNode);
		}
	}

	public List<String> getRange(long from, long to) throws IllegalArgumentException {
		if (to < from) {
			throw new IllegalArgumentException();
		}

		List<String> matchingChunks = new ArrayList<String>();

		// Run BFS and collect matching chunks for this time range
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root);

		while (queue.size() != 0) {
			Node current = queue.poll();
			
			System.out.format("Checking from %s to %s...\n", current.from, current.to);

			// Range check, don't continue if the node is out of range
			if (current.from > to || from > current.to) {
				continue;
			}
			if (current instanceof ChunkNode) {
				matchingChunks.add(((ChunkNode) current).storeKey);
			}

			for (Node node : current.children) {
				queue.add(node);
			}
		}

		return matchingChunks;
	}
	
	public void getCount(long from, long to) {
		// TODO
	}

	public void getMin(long from, long to) {
		// TODO
	}

	public void getMax(long from, long to) {
		// TODO
	}

	public void getSum(long from, long to) {
		// TODO
	}

	/**
	 * Rolls up the update to the metadata with respect to the newly stored leaf node
	 */
	private void updateMetadata(Node newNode) {
		Node parent = newNode.parent;
		while (parent != null) {
			if (parent.from == 0L) {
				parent.from = newNode.from;
			}
			parent.to = newNode.to;
			((MetaNode) parent).count++;
			
			parent = parent.parent;
		}
		// TODO: do something with min, max and sum
		// TODO: modularise by allowing stats operations to be plugged in (plugins should operate the way the tree operates)
	}
}

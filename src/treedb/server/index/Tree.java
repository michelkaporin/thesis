package treedb.server.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import treedb.server.Metadata;
import treedb.server.index.node.ChunkNode;
import treedb.server.index.node.Node;

/**
 * TODO: Consider the cases when the tree is in the middle of insert operation and other data is inserted/read from it
 */
public class Tree {
	private Node root;
	private int k; // maximum amount of children per node
	
	private List<Node> lastNodes; // Stores last node under each tree level, level 0 reserved to the leaf nodes
	
	public Tree(int k) {
		root = new Node();
		
		this.k = k;
		
		lastNodes = new ArrayList<Node>();
		lastNodes.add(root);
	}
	
	/**
	 * Inserts chunk into the leaf node, constructing k-ary tree in a bottom-up way
	 */
	public void insert(String key, Metadata metadata) {
		ChunkNode insertNode = new ChunkNode(metadata, key);
		
		int currentLevel = 0;
		
		// Check if parent node has available edges to store the node, if not
		Node lastLevelNode = lastNodes.get(currentLevel);
		if (lastLevelNode.children.size() < k) {
			lastLevelNode.children.add(insertNode);
			insertNode.parent = lastLevelNode;
			this.updateMetadata(insertNode);
			return;
		}

		Node previousNode = insertNode;
		while (lastLevelNode != null && lastLevelNode.children.size() >= k) {
			Node newLevelNode = new Node();
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
			Node lastRoot = root;
			root = new Node();
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

		List<String> matchingStorageKeys = new ArrayList<String>();

		// Run BFS and collect matching chunks for this time range
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root);

		while (queue.size() != 0) {
			Node current = queue.poll();

			// Range check, don't continue if the node is out of range
			if (!inRange(current, from, to)) {
				continue;
			}
			if (current instanceof ChunkNode) {
				matchingStorageKeys.add(((ChunkNode) current).storeKey);
			}

			for (Node node : current.children) {
				queue.add(node);
			}
		}

		return matchingStorageKeys;
	}
	
	public List<Metadata> getMetadata(long from, long to) {
		if (to < from) {
			throw new IllegalArgumentException();
		}

		List<Metadata> gatheredStatistics = new ArrayList<Metadata>();
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root);

		while (queue.size() != 0) {
			Node current = queue.poll();

			// Range check
			if (!inRange(current, from, to)) {
				continue;
			}
			
			// If current node is fully contained within the search range, then store its stats
			if ((current.metadata.to - current.metadata.from) <= (to - from)) {
				gatheredStatistics.add(current.metadata);
				continue;
			}

			// Else explore child nodes for the 
			for (Node node : current.children) {
				queue.add(node);
			}
		}

		return gatheredStatistics;
	}

	/**
	 * Rolls up the update to the metadata with respect to the newly stored leaf node
	 */
	private void updateMetadata(Node newNode) {
		Node parent = newNode.parent;
		while (parent != null) {
			if (parent.metadata.from == 0L) {
				parent.metadata.from = newNode.metadata.from;
			}
			parent.metadata.to = newNode.metadata.to;
			parent.metadata.count += newNode.metadata.count;
			parent.metadata.max = Math.max(parent.metadata.max, newNode.metadata.max);
			parent.metadata.min = Math.min(parent.metadata.min, newNode.metadata.min);
			parent.metadata.sum += newNode.metadata.sum;
			
			parent = parent.parent;
		}
		// TODO: do something with min, max and sum
		// TODO: modularise by allowing stats operations to be plugged in (plugins should operate the way the tree operates)
	}

	private boolean inRange(Node current, long from, long to) {
		if (current.metadata.from <= to && from <= current.metadata.to) {
			return true;
		}

		return false;
	}
}

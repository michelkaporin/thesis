package treedb.server.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import treedb.server.tree.node.ChunkNode;
import treedb.server.tree.node.MetaNode;
import treedb.server.tree.node.Node;

public class Tree {
	private MetaNode root;
	private int k; // maximum amount of children per node
	
	private List<MetaNode> lastNodes; // Stores last node under each tree level, level 0 reserved to the leaf nodes
	
	public Tree(int k) {
		root = new MetaNode();
		
		this.k = k;
		lastNodes = new ArrayList<MetaNode>();
		lastNodes.add(root);
	}
	
	/**
	 * Inserts chunk into the leaf node, constructing k-ary tree in a bottom-up way
	 */
public void insert(Chunk chunk, long from, long to) {
		ChunkNode insertNode = new ChunkNode();
		insertNode.chunk = chunk;
		insertNode.from = from;
		insertNode.to = to;
		
		int currentLevel = 0;
		
		// Check if parent node has available edges to store the node, if not
		MetaNode lastLevelNode = lastNodes.get(currentLevel);

		if (lastLevelNode.children.size() < k) {
			lastLevelNode.children.add(insertNode);
			this.updateMetadata(lastLevelNode, insertNode);
			return;
		}

		Node previousNode = insertNode;
		while (lastLevelNode != null && lastLevelNode.children.size() >= k) {
			MetaNode newLevelNode = new MetaNode();
			newLevelNode.children.add(previousNode);
			this.updateMetadata(newLevelNode, previousNode); // update metadata for newly created node
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
			MetaNode lastRoot = root;
			root = new MetaNode();
			root.children.add(lastRoot); // point last root to the new root
			root.children.add(previousNode); // point last created node to the new root
			this.updateMetadata(root, previousNode);
		}
	}

	public List<byte[]> getRange(long from, long to) throws IllegalArgumentException {
		if (to < from) {
			throw new IllegalArgumentException();
		}

		List<byte[]> matchingChunks = new ArrayList<byte[]>();

		// Run BFS and collect matching chunks for this time range
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root);

		while (queue.size() != 0) {
			Node current = queue.poll();

			// Range check
			if (current.from > to || from > current.to) {
				continue;
			}
			if (current instanceof ChunkNode) {
				matchingChunks.add(((ChunkNode) current).chunk.data);
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

	private void getRange(Node current, long from, long to, List<Node> chunks) {
		if (current.from < from) {
			return;
		}

		for (Node n : current.children) {
			getRange(n, from, to, chunks);
		}
		
		if (current.from > from && current.to < to) {
			chunks.add(current);
		}
	}

	/**
	 * Updates metadata about the stored nodes below in the tree
	 */
	private void updateMetadata(MetaNode existingNode, Node newNode) {
		if (existingNode.from == 0L) {
			existingNode.from = newNode.from;
		}
		existingNode.to += newNode.to;

		existingNode.count++;
		// TODO: do something with min, max and sum
	}
}

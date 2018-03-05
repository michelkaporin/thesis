package timecrypt.server.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import timecrypt.server.index.Metadata;
import timecrypt.server.index.MetadataConfiguration;
import timecrypt.server.index.node.ChunkNode;
import timecrypt.server.index.node.Node;

/**
 * TODO: Consider the cases when the tree is in the middle of insert operation and other data is inserted/read from it
 */
public class Tree {
	private Node root;
	private int k; // maximum amount of children per node
	private MetadataConfiguration metaConfig;
	
	private List<Node> lastNodes; // Stores last node under each tree level
	private int leavesCount;
	
	public Tree(int k, MetadataConfiguration metaConfig) {
		root = new Node();
		
		this.k = k;
		this.metaConfig = metaConfig;
		
		lastNodes = new ArrayList<Node>();
		lastNodes.add(root);

		leavesCount = 0;
	}

	public MetadataConfiguration getMetadataConfig() {
		return this.metaConfig;
	}
	
	/**
	 * Inserts chunk into the leaf node, constructing k-ary tree in a bottom-up way
	 */
	public void insert(String key, Metadata metadata) {
		ChunkNode insertNode = new ChunkNode(metadata, key);
		this.leavesCount++;
		
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
		while (lastLevelNode != null) {
			if (lastLevelNode.children.size() >= k) {
				Node newLevelNode = new Node();
				newLevelNode.children.add(previousNode);
				previousNode.parent = newLevelNode;
				lastNodes.set(currentLevel, newLevelNode); // update last node on the current level
				previousNode = newLevelNode; // Update reference to the node that needs to be connected later on
			} else {
				lastNodes.get(currentLevel).children.add(previousNode);
				previousNode.parent = lastNodes.get(currentLevel);
			}

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
		}

		this.updateMetadata(insertNode); // update tree metadata for newly created node
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
			if (current.metadata.from >= from && current.metadata.to <= to) {
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
	 * Checks that data to be inserted is newer then the last inserted chunk.
	 * Ensures that index is append-only.
	 */
	public boolean dataIsNewer(Metadata md) {
		if (md.from > lastNodes.get(0).metadata.from) {
			return true;
		}

		return false;
	}

	public int getLeavesCount() {
		return this.leavesCount;
	}

	/**
	 * Rolls up the update to the metadata with respect to the newly stored leaf node
	 */
	private void updateMetadata(Node newNode) {
		Node parent = newNode.parent;
		while (parent != null) {
			Metadata.updateMetadata(this.metaConfig, newNode.metadata, parent.metadata);
			parent = parent.parent;
		}
	}

	private boolean inRange(Node current, long from, long to) {
		if (current.metadata.from <= to && from <= current.metadata.to) {
			return true;
		}

		return false;
	}
}

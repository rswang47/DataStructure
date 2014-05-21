

/*
 * The class uses adjacent list to represent an undirected graph. It contains five fields. Static
 * list "Nodes" keeps track of all the objects of this class; HashMap "neighbours" stores each node's
 * adjacent node and corresponding edge length; "name" and "id" identify a node; "distance" records
 * current shortest path to the source node. All graph operations like setting neighbor, distance changing,
 * duplicated edge check, etc. are held by this class. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Vertex {
	public static ArrayList<Vertex> Nodes; //Store the entire graph information;
	private HashMap<Vertex, Integer> neighbours; //Adjacent list of neighbors with edge length to each neighbor as value;
	private String name;
	private int id;
	private int distance; //Store the distance to the source node;
	
	public Vertex(String name, int id, int s_id) {
		this.name = name;
		this.id = id;
		this.neighbours = new HashMap<Vertex, Integer>(); //Initiate neighbor list;
		
		/*
		 * Update graph information. If first node, initiate a new graph; else add this node
		 * to existed graph.
		 */
		if (Nodes == null) {
			Nodes = new ArrayList<Vertex>();
			Nodes.add(this);
		}
		else Nodes.add(this);
		
		if (id == s_id) this.distance = 0; // Initiate distance. If source node set 0, else set infinity;
		else this.distance = Integer.MAX_VALUE;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String toString() {
		return this.name;
	}
	
	public int getDis() {
		return this.distance;
	}
	
	public HashMap<Vertex, Integer> getNb() {
		return this.neighbours;
	}
	
	public void setNb(Vertex node, int weight) {
		neighbours.put(node, weight);
	}
	
	public void setDis(int n_dis) {
		this.distance = n_dis;
	}
	
	/* 
	 * This method check whether an edge to the
	 * target node has existed in the graph.
	 */
	public boolean isNb(Vertex node) {
		return neighbours.containsKey(node);
	}
	
	/*
	 * Check whether the graph is connected, using DFS algorithm.
	 * If all nodes in the graph can be reached from one node,
	 * the graph is connected.
	 */
	public static boolean isConnected(Vertex source, int num) {
		boolean[] visited = new boolean[num]; //Store visited node in avoid of rounded routing;
		
		Stack<Vertex> visiting = new Stack<Vertex> (); //A stack for nodes that are going to be visited;
		visiting.push(source); //Push the source node to the stack to begin traversing;
		while (!visiting.isEmpty()) {
			Vertex cur = visiting.pop(); //Visit the first node in going-to-visit stack;
			visited[cur.getId()] = true; //Mark the node as visited;
			for (Vertex v: cur.getNb().keySet()) {
				// Traverse the node's neighbor; push the unvisited ones into going-to-visit stack;
				if (visited[v.getId()] == false && !visiting.contains(v)) {
					visiting.push(v);
				}
			}
		}
		
		//Check connection. If find a node not visited yet, graph is not connected;
		for (boolean v: visited) {
			if (v == false) return false;
		}
		return true;
	}
	
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;



public class dijikstra {

	static Vertex[] Unset; //Store unsettled vertex;
	static ArrayList<Vertex> set = new ArrayList<Vertex> (); //Store settled vertex;
	
	static FibonacciHeap<Vertex> f_unset = new FibonacciHeap<Vertex> (); //Store unsettled vertex in a Fibonacci Heap;
	
	static HashMap<Vertex, FibonacciHeap.FibonacciNode<Vertex>> fv_Map = new HashMap<Vertex, 
			FibonacciHeap.FibonacciNode<Vertex>> (); //Map vertexes to their corresponding Fibonacci Node in the f-heap;
	
	public static void main(String[] args) {
		
		//Mode check. Check the first argument: if "-r" go random mode, else go user mode;
		if (args[0].equals("-r")) {
			int n = Integer.valueOf(args[1]); //Number of vertexes;
			
			double density = Double.valueOf(args[2]); //Density of edges;
			
			int source = Integer.valueOf(args[3]); //Source vertex id;
			Unset = new Vertex[n];
			initiate(n, density, source); //Initiate a graph;
			run_Dijikstra(Unset); //Run simple array;
			reset(source); //Reset;
			frun_Dijikstra(f_unset); //Run f-heap array;
		} else {
			String filename = new String(args[1]);
			String mode = new String(args[0]);
			initiate(filename, mode);
			
			//Check running mode. If "-s" go simple array, else if "-f" go f-heap;
			if (mode.equals("-s")) run_Dijikstra(Unset);
			else if (mode.equals("-f")) frun_Dijikstra(f_unset);
			
			//Standard output stream. File name is "result_file";
			try {
				PrintWriter res = new PrintWriter("result_file", "UTF-8");
				for (Vertex v: Vertex.Nodes) {
					String output = String.format("%d//cost from node %d to node %d", v.getDis(), set.get(0).getId(), 
							v.getId());
					System.out.println(output);
					res.println(output);
				}
				res.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Random mode. Initiate a graph with "n" nodes, "density" of edge density
	 * and source node id "source". Store nodes in simple array and f-heap respectively.
	 */
	public static void initiate(Integer n, double density, int source) {
		for (Integer i = 0; i<n; i++) {
			Vertex v = new Vertex(i.toString(), i, source);
			Unset[i] = v; //Simple array initiation;
			
			fv_Map.put(v, f_unset.insert(v, v.getDis())); //f-heap initiation;
		}
		int edge_num = (int) ((n*(n-1)/2)*density); //Calculate number of edges;
		int num = 0;
		Random rand = new Random();
		while (!Vertex.isConnected(Vertex.Nodes.get(source), n)) {
			while (num < edge_num) {
				int i = rand.nextInt(n);
				int j = rand.nextInt(n);
				Vertex v1 = Vertex.Nodes.get(i);
				Vertex v2 = Vertex.Nodes.get(j);
				if (v1.isNb(v2) || i == j) continue; //Check whether edge exists or indexes point to same node;
				else {
					int weight = rand.nextInt(1000) + 1;
					v1.setNb(v2, weight); //Set each end node as each other's neighbor;
					v2.setNb(v1, weight);
					num++;
				}
			}
		}	
	}
	
	/*
	 * User mode. Initiate the graph with user supplied file.
	 */
	public static void initiate(String filename, String mode) {
		File file = new File(filename); //Open the file;
		if (!file.exists()) {
			System.err.println("Wrong filename!"); //If file does not exist, exit;
			System.exit(0);
		}
		Scanner filescan;
		try {
			filescan = new Scanner(file); //Scan the file;
			if (!filescan.hasNextLine()) {
				System.err.println("Wrong contents!");
				System.exit(0);
			}
			int source = Integer.valueOf(filescan.nextLine()); //Get source node id in first line;
			
			StringTokenizer graph = new StringTokenizer(filescan.nextLine(), " "); //Get number of nodes and edges;
			int num = Integer.parseInt(graph.nextToken());
			
			//Simple array mode initiation or f-heap mode initiation;;
			if (mode.equals("-s")) {
				Unset = new Vertex[num];
				for (Integer i = 0; i<num; i++) {
					Unset[i] = new Vertex(i.toString(), i, source);
				}
			} else if (mode.equals("-f")) {
				for (Integer i = 0; i<num; i++) {
					Vertex v = new Vertex(i.toString(), i, source);
					fv_Map.put(v, f_unset.insert(v, v.getDis()));
				}
			}
			
			int edge_num = Integer.parseInt(graph.nextToken());
			int edge_counter = 0; //Count the number of edges that have been set;
			while (filescan.hasNextLine()) {
				StringTokenizer edges = new StringTokenizer(filescan.nextLine(), " ");
				int i = Integer.parseInt(edges.nextToken()); //Node id of edge ends;
				int j = Integer.parseInt(edges.nextToken());
				int weight = Integer.parseInt(edges.nextToken()); //Weight of the edge;
				Vertex v1 = Vertex.Nodes.get(i);
				Vertex v2 = Vertex.Nodes.get(j);
				if (v1 == v2 || v1.isNb(v2)) continue; //Check duplication.
				v1.setNb(v2, weight);
				v2.setNb(v1, weight);
				edge_counter++;
			}
			if (edge_counter != edge_num || !Vertex.isConnected(Vertex.Nodes.get(source), num)) {
				// If actual edge number is wrong or graph is not connected, throw error and exit;
				System.err.println("Wrong initiation!");
				System.exit(0);
			}
		} catch (Exception e) {
		    System.err.println("Application exception: " + e.toString());
		    e.printStackTrace();
		}
	}
	
	/*
	 * Method applying dijikstra algorithm with simple array.
	 */
	public static void run_Dijikstra(Vertex[] Unset) {
		long start = System.currentTimeMillis();
		
		int count = 0; //Record the number of nodes that have been checked and removed;
		
		//Iterate until all nodes are settled;
		while (count < Unset.length) {
			/*
			 * Remove shortest path; add node to settled list; and neighbor path relax;
			 */
			Vertex U = find_Min(Unset, count);
			set.add(U);
			for (Vertex v: U.getNb().keySet()) {
				relax_Path(U, v);
			}
			count++;
		}
		long stop = System.currentTimeMillis();
		System.out.printf("Time for simple array: %d%n", stop-start);
	}
	
	/*
	 * Method to find and remove the node with shortest
	 * path in Unsettled list.
	 */
	private static Vertex find_Min(Vertex[] Unset, int count) {
		int minDis = Unset[count].getDis(); //get the distance of first node in unsettled list;
		
		for (int i = count+1; i < Unset.length; i++) {
			if (Unset[i].getDis() < minDis) {
				Vertex temp = Unset[count]; //If find shorter path, exchange the value so that smaller value is always on top;
				Unset[count] = Unset[i];
				Unset[i] = temp;
				minDis = Unset[count].getDis();
			}
		}
		return Unset[count];
	}
	
	/*
	 * Relax vertex V's path to source. V is a neighbor of U.
	 */
	private static void relax_Path(Vertex U, Vertex V) {
		int old_Dis = V.getDis();
		int new_Dis = U.getDis() + U.getNb().get(V);
		if (old_Dis > new_Dis) {
			V.setDis(new_Dis);
		}
	}
	
	/*
	 * Dijikstra algorithm with f-heap.
	 */
	public static void frun_Dijikstra(FibonacciHeap<Vertex> funset) {
		long start = System.currentTimeMillis();
		while (!funset.isEmpty()) {
			Vertex U = funset.deleteMin().getElem();
			set.add(U);
			for (Vertex v: U.getNb().keySet()) {
				frelax_Path(U, v, funset);
			}
		}
		long stop = System.currentTimeMillis();
		
		System.out.printf("Time for f-heap: %d%n", stop-start);
	}
	
	private static void frelax_Path(Vertex U, Vertex V, FibonacciHeap<Vertex> funset) {
		int old_Dis = V.getDis();
		int new_Dis = U.getDis() + U.getNb().get(V);
		if (old_Dis > new_Dis) {
			V.setDis(new_Dis);
			funset.decreaseKey(fv_Map.get(V), new_Dis);
		}
	}
	
	/*
	 * Reset the graph, restoring distance to infinity.
	 */
	private static void reset(int source) {
		set.clear();
		for (Vertex v: Vertex.Nodes) {
			if (v.getId() != source) v.setDis(Integer.MAX_VALUE);
			else v.setDis(0);
		}
	}
	
}

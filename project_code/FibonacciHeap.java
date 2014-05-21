

/*
 * This is an implementation of Fibonacci Heap. Basically it supports deleteMin, decreaseKey and insert
 * operations. The nested class FibonacciNode defines nodes stored in the heap. Each node holds eight
 * fields: its degree, its mark, the element it stores, its child, two siblings and parent, and its key.
 * Each heap holds nodes, size and a point to minElement.
 */

import java.util.ArrayList;
import java.util.NoSuchElementException;

public final class FibonacciHeap<T> {
	public static final class FibonacciNode<T> {
		private int degree = 0;
		private boolean isMarked = false;
		private T elem;
		private FibonacciNode<T> child;
		private FibonacciNode<T> next;
		private FibonacciNode<T> prev;
		private FibonacciNode<T> parent;
		private int key;
		
		private FibonacciNode(T elem, int key) {
			this.next = this.prev = this;
			this.elem = elem;
			this.key = key;	
		}
		
		public T getElem() {
			return elem;
		}
		
		public void setElem(T new_elem) {
			this.elem = new_elem;
		}
		
		public int getKey() {
			return this.key;
		}
	}
	
	private FibonacciNode<T> minElem = null;
	private int size = 0;
	
	public FibonacciNode<T> insert(T elem, int key) {
		FibonacciNode<T> node = new FibonacciNode<T> (elem, key);
		minElem = mergeList(minElem, node); //Treat new element as a skeleton tree and do the merging;
		size++;
		return node;
	}
	
	public FibonacciNode<T> getMin() {
		/*
		 * Check whether heap is empty. If not return minElem;
		 */
		if (isEmpty()) {
			throw new NoSuchElementException("Heap is Empty.");
		}
		return minElem;
	}
	
	public boolean isEmpty() {
		return minElem == null;
	}
	
	/*
	 * Delete minElem from heap. Tree reconstruction happens here, too.
	 */
	public FibonacciNode<T> deleteMin() {
		if (isEmpty()) {
			throw new NoSuchElementException("Heap is Empty.");
		}
		
		size--;
		
		FibonacciNode<T> min = minElem; // Store the point to minElem because if will be deleted;
		
		/*
		 * If roots contain only minElem, then merely set minElem null. Else drag minElem out of
		 * roots link by linking its siblings to each other, and arbitrarily reset new minElem.
		 */
		if (minElem.next == minElem) minElem = null;
		else {
			minElem.next.prev = minElem.prev;
			minElem.prev.next = minElem.next;
			minElem = minElem.next;
		}
		
		/*
		 * If old minElem had children, take them out and merge into root nodes. Set parent field
		 * of these children as null.
		 */
		if (min.child != null) {
			FibonacciNode<T> cur = min.child; //Store the start point in avoid of round routing;
			do {
				cur.parent = null;
				cur = cur.next;
			} while(cur != min.child);
		}
		minElem = mergeList(minElem, min.child);
		
		if (minElem == null) return min; //If no nodes remained, we are done;
		
		/*
		 * Reconstructing remaining root nodes. Merge nodes with same degree together in pairs until no
		 * root node has same degree.
		 */
		ArrayList<FibonacciNode<T>> toVisit = new ArrayList<FibonacciNode<T>> (); //Store root nodes to be visited;
		
		ArrayList<FibonacciNode<T>> nodeTable = new ArrayList<FibonacciNode<T>> (); //Store root nodes with certain degree;
		
		for (FibonacciNode<T> cur = minElem; toVisit.isEmpty() || toVisit.get(0) != cur; cur = cur.next) {
			toVisit.add(cur);
		}
		
		/*
		 * Iterate through every root node.
		 */
		for (FibonacciNode<T> node: toVisit) {
			/*
			 * Iterate until no root node has same degree.
			 */
			while (true) {
				while (node.degree >= nodeTable.size())
					nodeTable.add(null); //Extend the list to store node with corresponding degree;
				
				/*
				 * If no other node has same degree, push the node into corresponding slot of its degree
				 * and break iteration.
				 */
				if (nodeTable.get(node.degree) == null) {
					nodeTable.set(node.degree, node);
					break;
				}
				
				FibonacciNode<T> eNode = nodeTable.get(node.degree); //Get node with same degree;
				nodeTable.set(node.degree, null);
				
				FibonacciNode<T> small = (eNode.key < node.key)?eNode:node; //Compare keys and set node with smaller one as top;
				FibonacciNode<T> big = (eNode.key < node.key)?node:eNode;
				
				big.next.prev = big.prev; //Drag node with bigger key out of root node link;
				big.prev.next = big.next;
				
				big.next = big.prev = big; //Make the node skeleton node. Merge it with smaller node's child;
				small.child = mergeList(small.child, big);
				
				big.parent = small;
				big.isMarked = false; //New child is marked as not having lost child;
				
				small.degree++; //Degree of smaller node increase;
				
				node = small; //Continue reconstructing with smaller node;
			}
			
			if (node.key <= minElem.key) minElem = node; //Check minElem if find smaller key;
		}
		
		return min;
	}
	
	/*
	 * Decrease the key of certain node. If new key is smaller than its parent's key,
	 * cut it out and merge into root node link. If new key is smaller than minElem,
	 * set as new minElem.
	 */
	public void decreaseKey(FibonacciNode<T> node, int new_key) {
		node.key = new_key;
		if (node.parent != null && node.key <= node.parent.key) cutNode(node);
		
		if (node.key <= minElem.key) minElem = node;
	}
	
	/*
	 * Cascading cut.
	 */
	private void cutNode(FibonacciNode<T> node) {
		node.isMarked = false; //Set new cut node's mark as false;
		if (node.parent == null) return; //Check parent. If null, we are done;
		if (node.next != node) {
			node.next.prev = node.prev; //If not the only child, drag out of sibling link;
			node.prev.next = node.next;
		}
		
		/*
		 * If parent points to this, arbitrarily set new child point, or set null if no child remained.
		 */
		if (node.parent.child == node) {
			if (node.next != node) node.parent.child = node.next;
			else node.parent.child = null;
		}
		
		node.parent.degree--;
		node.prev = node.next = node; //Make this skeleton node and merge into root node link;
		minElem = mergeList(minElem, node);
		
		if (node.parent.isMarked) cutNode(node.parent); //If parent is marked true, cascade cutting;
		else node.parent.isMarked = true; //Else, mark parent as true;
		
		node.parent = null;
	}
	
	/*
	 * Merge two nodes together. Implemented in insert, deletMin, decreaseKey.
	 */
	private static <T> FibonacciNode<T> mergeList(FibonacciNode<T> fn_one, FibonacciNode<T> fn_two) {
		if (fn_one == null || fn_two == null) return fn_one != null?fn_one : fn_two;
		else {
			FibonacciNode<T> next = fn_one.next; //Cache the node because it will be overwritten;
			fn_one.next = fn_two.next;
			fn_two.next.prev = fn_one;
			fn_two.next = next;
			fn_two.next.prev = fn_two;
		}
		
		return fn_one.key < fn_two.key?fn_one : fn_two; //Return point to node with smaller key as minElem;
	}
}

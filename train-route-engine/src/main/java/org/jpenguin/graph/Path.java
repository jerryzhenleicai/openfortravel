package org.jpenguin.graph;

import java.util.Iterator;
import java.util.LinkedList;


public class Path implements Comparable {
    LinkedList nodes; // 0: start node, (size -1): end node
    double cost; // sum of all edges' cost

    public Path() {
        nodes = new LinkedList();
        cost = 0.0;
    }

    public int compareTo(Object o) {
        double diff = this.getCost() - ((Path) o).getCost();
        if (diff < 0) {
            return -1;
        }
        if (diff > 0) {
            return 1;
        }
        return 0;
    }

    public String toString() {
        return nodes.toString();
    }

    // One of the nodes (except src and dest ones) satisfies the given predicate
    public boolean innerNodeSatisfies(UnaryPredicate pred) {
        if (nodes.size() < 3) { // no inner node
            return false;
        }

        Iterator begin;
        begin = nodes.iterator();
        begin.next();
        while (true) {
			Object node = begin.next();
        	// do not check last one 
        	if(!begin.hasNext())
        		break;
        	if (pred.isTrueOn(node))
        		return true;
        }
        return false;
    }

    /**
     *   prepend a new node to the path
     *  @param data of the node
     */
    public void prependNode(Object node, double edge_cost) {
        nodes.addFirst(node);
        cost += edge_cost;
    }

    // append a new node to the path
    public void appendNode(Object node, double edge_cost) {
        nodes.addLast(node);
        cost += edge_cost;
    }

    /**
     *  length equals no of nodes - 1
     */
    public int getLength() {
        return nodes.size() - 1;
    }

    public Object getDestination() {
        return nodes.getLast();
    }

    public Object getSource() {
        return nodes.getFirst();
    }

    public double getCost() {
        return cost;
    }
    
    public Iterator getNodes() {
    	return nodes.iterator();
    }
    
}

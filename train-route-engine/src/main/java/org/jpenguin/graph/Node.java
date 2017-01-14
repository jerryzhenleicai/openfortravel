/*
ConceptScout: Discovery and Grouping of Semantic Concepts from Web Pages
Copyright (C) 2002  Zhenlei Cai (zcai@jpenguin.org)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.jpenguin.graph;

import java.util.*;


/**
 * To speed up looking up a node's edge leading to a particular node,
 * out and in edges are stored in hash maps with the neigboring node as
 * keys and the edge itself as values
 */
public class Node {
    static final int AVERAGE_OUT_DEGREE = 16;

    /** node's client data */
    Object node_data;

    /** outgoing edges */
    HashMap out_edges;
    HashMap in_edges; // incoming edges

    /** reserved for storing other info */
    Object aux_info;

    /**
     *  these are for A* search, used for adding to the estimated cost for priority Q.
     *   to speed up search to and from certain (top N) nodes.
     */
    public double[] to_top_n_min_cost; // min precomputed cost to the Top N nodes
    public double[] from_top_n_min_cost;

    public Node(Object data) {
        node_data = data;
        out_edges = new HashMap(AVERAGE_OUT_DEGREE, 0.8F);
        in_edges = new HashMap(AVERAGE_OUT_DEGREE, 0.8F);
        aux_info = null;
        to_top_n_min_cost = from_top_n_min_cost = null;
    }

    /**
     * @param data node data (looking up a node on this data is fast)
     * @param aux node extra info
     */
    public Node(Object data, Object aux) {
        this(data);

        aux_info = aux;
    }

    public int hashCode() {
        return node_data.hashCode();
    }

    public Object getNodeData() {
        return node_data;
    }

    public Object getAuxInfo() {
        return aux_info;
    }

    /**
     * this implies no two nodes can contain data with same value or values are equal.
     */
    public boolean equals(Object n) {
        return node_data.equals(((Node) n).node_data);
    }

    public Iterator getOutEdges() {
        return out_edges.values().iterator();
    }

    public Iterator getInEdges() {
        return in_edges.values().iterator();
    }

    /**
     * return the edge that leads from this node to dest node, null if none.
     */
    public Edge edgeToNode(Node dest) {
        return (Edge) out_edges.get(dest);
    }

    /**
     * return the edge that leads from src to this node, null if none.
     */
    public Edge edgeFromNode(Node src) {
        return (Edge) in_edges.get(src);
    }

    public String toString() {
        return node_data.toString();
    }

    /**
     * add an edge that starts from this node and end at another node.
     *
     *
     * @param edge the edge to add
     */
    void addOutEdge(Edge edge) {
        if (out_edges.put(edge.getTo(), edge) != null)
        	throw new IllegalStateException("Cannot have more than 1 edge to same neighbor node " 
        				+ edge.getFrom().toString() + " to " + edge.getTo().toString());
    }

    /**
     * add an edge that starts from another node and end at this node.
     *
     *
     * @param edge the edge to add
     */
    void addInEdge(Edge edge) {
        if (in_edges.put(edge.getFrom(), edge) != null)
    		throw new IllegalStateException("Cannot have more than 1 edge from the same neighbor node " 
    				+ edge.getFrom().toString() + " to " + edge.getTo().toString());
    }

    /**
     * for A* search
     *
     * @param n_top_nodes
     *
     * @see
     */
    public void setNumTopNNodes(int n_top_nodes) {
        from_top_n_min_cost = new double[n_top_nodes];
        to_top_n_min_cost = new double[n_top_nodes];
    }

    /**
     * Method declaration
     *
     *
     * @param top_n_node_index
     * @param cost
     *
     * @see
     */
    public void setMinCostFromTopNode(int top_n_node_index, double cost) {
        from_top_n_min_cost[top_n_node_index] = cost;
    }

    /**
     * Method declaration
     *
     *
     * @param top_n_node_index
     * @param cost
     *
     * @see
     */
    public void setMinCostToTopNode(int top_n_node_index, double cost) {
        to_top_n_min_cost[top_n_node_index] = cost;
    }
}

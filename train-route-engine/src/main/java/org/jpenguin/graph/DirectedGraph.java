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


/**
 * Possibly disconnected directed graph where edges are stored in adjacency lists.
 * No more than one edge can connect give two nodes in either direction (that is from one node to 
 * one of its neighbor nodes, only one edge can be used).
 * Call addEdge (Object from, Object to) to add both nodes and edges.
 */
import java.util.*;


public class DirectedGraph {
    /** map mapping from node data to nodes, so looking up node from its data is fast */
    protected HashMap<Object, Node> clientDataToNodeMap;
    protected java.util.Vector edges;

    /**
     * construct a empty graph
     */
    public DirectedGraph() {
        clientDataToNodeMap = new HashMap<Object, Node> ();
        edges = new java.util.Vector();
    }

    /**
     * add an edge with the given cost (weight)
     */
    public void addEdge(Node from, Node to, double cost) {
        Edge edge = new Edge(from, to, cost);
        addEdge(edge);
    }

    /**
     * Number of nodes in the graph
     */
    public int getNumberNodes() {
        return clientDataToNodeMap.size();
    }

    /**
     * add a pre-built edge
     */
    public void addEdge(Edge edge) {
        edge.getFrom().addOutEdge(edge);
        edge.getTo().addInEdge(edge);
        edges.add(edge);
    }

    /**
     * add a node with given node data
     */
    public Node addNode(Object data) {
        return addNode(data, null);
    }

    /**
     * add a node with given node data and auxiliary information
     */
    public Node addNode(Object data, Object aux_info) {
        Node node;
        Node n;

        node = new Node(data, aux_info);
        n = (Node) clientDataToNodeMap.put(data, node);

        if (n == null) {
            return node;
        } else { // node already there, adding failed
            return n;
        }
    }

    /**
     * get the node that contains the given data,  will return null if nothing is found
     */
    public Node getNode(Object data) {
        Node n;
        n = clientDataToNodeMap.get(data);
        return n;
    }

    public Collection<Node> getNodes() {
        return clientDataToNodeMap.values();
    }
}

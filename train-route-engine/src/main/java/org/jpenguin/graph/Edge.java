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
 * A directed edge linking two nodes in a graph.
 */
public class Edge {
    private Node from;
    private Node to;
    private double cost; // edge cost
    Object aux_info; // auxilary info about the edge can be stored

    private Edge() {
    	// disable default constr
    }
    /**
     * Constructor.
     *
     * @param f from node
     * @param t to node
     * @param c edge cost/weight
     *
     */
    public Edge(Node f, Node t, double c) {
        setFrom(f);
        setTo(t);
        setCost(c);
        aux_info = null;
    }

    /**
     * Constructor.
     *
     * @param f from node
     * @param t to node
     * @param c edge cost/weight
     * @param aux auxialary info about edge
     */
    public Edge(Node f, Node t, double c, Object aux) {
        this(f, t, c);

        aux_info = aux;
    }

    public Object getAuxInfo() {
        return aux_info;
    }

    public void setAuxInfo(Object aux) {
        aux_info = aux;
    }

    public int hashCode() {
    	throw new IllegalStateException("One should never call Edge hashCode()");
    }

    public boolean equals(Object o) {
    	throw new IllegalStateException("One should never call Edge equal");
    }

	public void setFrom(Node from) {
		if (from == null) {
			throw new IllegalArgumentException("Null from node in edge");
		}
		this.from = from;
	}

	public Node getFrom() {
		return from;
	}

	public void setTo(Node to) {
		if (to == null) {
			throw new IllegalArgumentException("Null to node in edge");
		}
		this.to = to;
	}

	public Node getTo() {
		return to;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getCost() {
		return cost;
	}
}

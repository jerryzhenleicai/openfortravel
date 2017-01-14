package org.jpenguin.graph;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;


/**
 * Subclass of DirectedGraph which supports single source shortest path
 * calc by Dijkstra's algorithm
 * Cal addEdge (Object from, Object to) to add both nodes and edges.
 *
 * @author     zcai
 */
public class DijkstraGraph extends DirectedGraph {
    boolean a_star_search_mode = false;
    private boolean debugging = false;
    Node source_node;
    Node destination_node;

    /**
     * any node satisfies this predicate will be excluded during search
     */
    UnaryPredicate nodeExclusionPredicate;

    /**
     * in some cases the end node is not exactly known, but there's only a condition that when met by a node signifies it's a
     * end node. This predicate describe under what condition we terminate search.
     */
    UnaryPredicate terminate_search_predicate;

    /** used in conjunction with terminate_search_predicate, if larger than 1, then we don't terminate immediately after found 1st end node */
    int num_dest_to_reach;

    /**
     * If the bigger city is one of the top cities, enable A star search.
     * Eg, find path from Beijing->Hefei, Beijing is top node 0, then set topNodeIsSource to true, a_star_search_top_node_index to 0.
     * If search Hefei->Beijing, then set to false.
     */
    boolean topNodeIsSource = true;
    int a_star_search_top_node_index;

    /**
     * controls whether the search starts from source node or destination node, if from source node, set to true,
     * and follow the links forwards (i.e. out edges), else follow links backwards (in edges). In some cases we want to search
     * backwards, e.g. find the path from a big city to small city. Because the small city has less train lines to try, so we
     * use the nodes corresponding to smaller city's LSPs as destination nodes from which to start search (terminate condition is
     * set to the node being the big city's LSP).
     */
    boolean forward_search;

    /** from which node (either source or dest) to start search, if forward_search is true, then this node is the same as source node */
    Node start_node;

    /** for reset source node and recompute */
    boolean shortest_path_computed;
    Vector final_nodes_reached;

    /** If true, abort search if the cost of the curr partial path is already greater than threshold */
    boolean use_cost_threshold;
    private double costThreshold;
    /** sorted by graph cost, no duplicate key allowed */
    SortedMap<Double, Node > shortestCostToNodeMap;

    public DijkstraGraph() {
        super();
        source_node = null;
        shortestCostToNodeMap = new TreeMap<Double, Node>();
        shortest_path_computed = false;
        forward_search = true;
        terminate_search_predicate = null;
        nodeExclusionPredicate = null;
    }

    /**
     * If the bigger node is one of the top nodes, enable A star search.
     *
     * @param  top_is_src  whether we are traveling from the bigger city to the smaller one.
     * Eg, find path from Beijing->Hefei, Beijing is top node 0, then set to true, a_star_search_top_node_index to 0.
     * If search Hefei->Beijing, then set to false.
     * @param  top_index   The new aStarSearchMode value
     */
    public void setAStarSearchMode(boolean top_is_src, int top_index) {
        a_star_search_mode = true;
        topNodeIsSource = top_is_src;
        a_star_search_top_node_index = top_index;
    }

    /**
     * disable A star search
     */
    public void unsetAStarSearchMode() {
        a_star_search_mode = false;
    }

    /**
     * In a predicated search, return the exact set of end nodes found
     */
    public Vector getFinalNodesReached() {
        // this means there's from src node it's not possible to reach dest node
        if (final_nodes_reached.size() == 0) {
            return null;
        }
        return final_nodes_reached;
    }

    /**
     * set from which node to start search, this can be either source or dest node depending on search direction
     * @param  destination_data the client data based on which to determine the dest node
     * @throws  UnknownNodeExcepton
     * @see
     */
    public void setStartNode(Object start_data) throws UnknownNodeExcepton {
        // find the node
        start_node = getNode(start_data);
        if (start_node == null) {
            throw new UnknownNodeExcepton(start_data);
        }
    }

    /**
     * Clear results from last search to prepare for a fresh new search
     */
    void resetAll() {
        // clear flag so the shortest path will have to be recomputed
        shortest_path_computed = false;

        // clear the search related info (eg, path_found flag and costs) for each node
        Iterator it = this.clientDataToNodeMap.values().iterator();
        while (it.hasNext()) {
            Node nd = (Node) (it.next());
            ((DijkstraNodeInfo) (nd.aux_info)).clear();
        }

        // clear cost priority queue
        shortestCostToNodeMap.clear();

        // make start_node the first known node
        DijkstraNodeInfo node_info = (DijkstraNodeInfo) start_node.aux_info;
        node_info.cost_to_prev_node_on_path = 0.0;
        node_info.shortest_cost_from_src = 0.0;
        node_info.prev_node_on_shortest_path = null;
        // clear final nodes reached
        final_nodes_reached = new Vector();
    }

    /**
     * set whether search starts from source node or dest
     * @param  f if true then forward search (ie, start from source node)
     */
    public void setPathSearchDirection(boolean f) {
        forward_search = f;
    }

    /**
     * Override parent class's method, force creation of Dijkstra info as aux_info for node
     */
    public Node addNode(Object data) {
        return (addNode(data, new DijkstraNodeInfo()));
    }

    /**
     * This can only be called after computeShortestPaths() is called. Given a end node, return the shorted path from start node to it.
     * @param  end_node must be one of the final nodes reached in case of predicate based search.
     */
    public Path getShortestPath(Node end_node) {
        Node curr_node_on_path = end_node;
        Path path = new Path();
        DijkstraNodeInfo info;

        while (curr_node_on_path != null) {
            info = (DijkstraNodeInfo) curr_node_on_path.aux_info;

            // if we search backward, then those nodes chained together by prev_node_on_shortest_path
            // will form a path that's a reverse of path get by forward search
            // add curr node to path
            if (forward_search == true) {
                path.prependNode(curr_node_on_path.node_data, info.cost_to_prev_node_on_path);
            } else {
                path.appendNode(curr_node_on_path.node_data, info.cost_to_prev_node_on_path);
            }

            // go to prev node on path
            curr_node_on_path = info.prev_node_on_shortest_path;
        }
        return path;
    }

    /**
     * Terminate search if the cost of the curr partial path is already greater than threshold
     *
     * @param  max_cost  The new costThreshold value
     */
    public void setCostThreshold(double max_cost) {
        costThreshold = max_cost;
        use_cost_threshold = true;
    }

    public double getCostThreshold() {
        return costThreshold;
    }

    /**
     * disable threshold for search
     */
    public void clearCostThreshold() {
        use_cost_threshold = false;
    }

    /**
     * set the condition based on which end nodes are determined,
     * @param pred a predicate on nodes that describe the condition
     * @param num_end_nodes how many end nodes that satisfy the predicate condition are found before stop searching
     */
    public void setTerminateCondition(UnaryPredicate pred, int num_end_nodes) {
        terminate_search_predicate = pred;
        num_dest_to_reach = num_end_nodes;
    }

    public void setNodeExclusionPredicate(UnaryPredicate pred) {
        nodeExclusionPredicate = pred;
    }

    /**
     * no longer exclude any nodes
     * @param pred
     */
    public void clearNodeExclusionPredicate() {
        nodeExclusionPredicate = null;
    }

    /**
     * set the predicate based on which end nodes are determined, only 1 end node will be found
     * @param pred a predicate on nodes that describe the condition
     */
    public void setTerminateCondition(UnaryPredicate pred) {
        setTerminateCondition(pred, 1);
    }

    /**
     * compute shortest paths from the source node or (if forward search if false, from the dest node backwards) to all other nodes. Unless
     * terminate condition is set in which case search stops at the first node (or a predefined number of nodes)
     * which meet the condition. If A* search, reset mode after done.
     * @see setTerminateCondition
     */
    public void computeShortestPaths() {
        Edge edge;
        DijkstraNodeInfo node_info;
        Node nearest_node;
        DijkstraNodeInfo nearest_node_info;
        Node neighbor_node;

        // the priority queue contains nodes who have been found reachable from start node, together with their distances 
        // from the start node (or in A star search mode, the estimate of the shortest path between start and end nodes that
        // goes through the node).
        boolean stop = false;

        // reset variables set from previous run
        resetAll();

        // Initilialize riority queue
        Double initCost;
        if (a_star_search_mode) {
            initCost = getCostEstimate(start_node);
        } else {
            initCost = 0.0;
        }

        shortestCostToNodeMap.put(initCost, start_node);

        // test if the start node is already a end node
        if (terminate_search_predicate != null) {
            if (terminate_search_predicate.isTrueOn(start_node.node_data)) {
                final_nodes_reached.add(start_node);
                // no need to find more end nodes since start node is dest
                stop = true;
            }
        }

looponpaths: 
        // iterate until no more path shortening is possible
        while (stop == false) {
            // this flag will be set to false below if more path shortening is possible
            stop = true;

            // find an unexplored node with the smallest cost (estimate)
            // note we may have  multiple copies of the same node in the queue,
            // the one copy that has the lowest cost will be popped first and marked as
            // path found (explored). The other copies will be skipped in this while loop
            /// when they are later popped out as the node has been marked as explored , see below
            while (true) {
                if (shortestCostToNodeMap.isEmpty()) {
                    break looponpaths;
                }

                // pop out the the node with the shortest cost so far (or estimated path cost if A star search)
                Double shortest = shortestCostToNodeMap.firstKey();
                nearest_node = shortestCostToNodeMap.get(shortest);
                shortestCostToNodeMap.remove(shortest);

                // skip node if it's excluded and not the start node
                if (nodeExclusionPredicate != null) {
                    if ((nearest_node != start_node) && nodeExclusionPredicate.isTrueOn(nearest_node.node_data)) {
                        continue;
                    }
                }

                nearest_node_info = (DijkstraNodeInfo) nearest_node.aux_info;
                // we may have multiple copies of the same node in the queue, so this node may have already been explored before, see above
                if (nearest_node_info.path_found == false) {
                    // there's still at least this node that is unexplored
                    stop = false;
                    break;
                }
            }

            // mark the popped up node a  known node
            nearest_node_info.path_found = true;
            // if we are doing predicate based search, see if the node meet the terminating condition (hence is a end node)
            if ((nearest_node != start_node) && (terminate_search_predicate != null)) {
                if (terminate_search_predicate.isTrueOn(nearest_node.node_data)) {
                    final_nodes_reached.add(nearest_node);
                    // we have found enough end nodes, terminate the whole thing
                    if (final_nodes_reached.size() >= num_dest_to_reach) {
                        break looponpaths;
                    } else {
                        // look for next end node
                        continue;
                    }
                }

                // cost exceeded the threshold, abort search 
                if (use_cost_threshold && (nearest_node_info.shortest_cost_from_src > getCostThreshold())) {
                    break looponpaths;
                }
            }

            // for each unexplored node adjacent to the newly explored node
            Iterator edge_enum = ((forward_search == true) ? nearest_node.getOutEdges() : nearest_node.getInEdges());
            while (edge_enum.hasNext()) {
                edge = (Edge) edge_enum.next();
                neighbor_node = ((forward_search == true) ? edge.getTo() : edge.getFrom());
                node_info = (DijkstraNodeInfo) neighbor_node.aux_info;
                // node explored already?
                if (node_info.path_found) {
                    continue;
                }

                // if using new node reduce the cost for the unexplored node
                if ((nearest_node_info.shortest_cost_from_src + edge.getCost()) < node_info.shortest_cost_from_src) {
                    // see if the unexplored node has actually being the neighbor of some previously explored nodes (so its cost
                    // is not infinity as if it has never been touched)
                    // update cost and prev node pointer
                    node_info.shortest_cost_from_src = nearest_node_info.shortest_cost_from_src + edge.getCost();
                    node_info.prev_node_on_shortest_path = nearest_node;
                    node_info.cost_to_prev_node_on_path = edge.getCost();

                    // insert the updated neighbor node to Q without removing the old copy
                    // Cannot use java.util.TreeSet.remove because TreeSet'remove () do not work for non-first node due to
                    // comparator'semantics (return less if equal), remove() only removes the node if it's the left most node with equal cost
                    // or miss entirely,
                    // insert a copy of the unexplored node with updated cost to the prior Queue. This will cause multiple records of the
                    // same node in the queue, but we will only extract the copy with the lowest cost in the popping step
                    // and whenever a node is declared as explored (i.e. shortest path found for it) all its other
                    // copies will be disabled and not popped ever, see above
                    // just prepare another mapping for the node and insert that mapping into the Q with new path cost info,
                    Double newCost = new Double(getCostEstimate(neighbor_node));
                    // avoid duplicates
                    while (shortestCostToNodeMap.containsKey(newCost)) {
                        newCost = newCost + 0.00000001;
                    }
                    shortestCostToNodeMap.put(newCost, neighbor_node);
					/*
					if (debugging)
                                 ChineseUtils.printlnUnicode("Edge cost " + edge.getCost() + " new cost from src " + node_info.shortest_cost_from_src
                    + "; Insert : " +    (neighbor_node.node_data.toString())
                      + " to Q with cost est." + getCostEstimate (neighbor_node));
                      */
                }
            }
        }

        // reset A* mode
        a_star_search_mode = false;
        shortest_path_computed = true;
    }

    /**
     *  Gets the costEstimate attribute of the DijkstraGraph object
     *
     * @param  node  Description of the Parameter
     * @return       The costEstimate value
     */
    double getCostEstimate(Node node) {
        DijkstraNodeInfo f = (DijkstraNodeInfo) node.aux_info;

        // make sure under curr search direction the top node is indeed a target node (either src or dest)
        if (a_star_search_mode && topNodeIsSource && !forward_search) {
            return f.shortest_cost_from_src + node.from_top_n_min_cost[a_star_search_top_node_index];
        } else if (a_star_search_mode && !topNodeIsSource && forward_search) {
            return f.shortest_cost_from_src + node.to_top_n_min_cost[a_star_search_top_node_index];
        } else {
            return f.shortest_cost_from_src;
        }
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    public boolean isDebugging() {
        return debugging;
    }

    /**
    * Aux info for a Node which holds a dobule value as the estimate of shortest path
    * to source node and previous node on the path, needed by Dijkstra's algorithm
    * <p>
    *
    * @author     zcai
    * @created    December 27, 2002
    */
    public class DijkstraNodeInfo {
        // if true then cost estimate and prev node are final
        public boolean path_found;

        // if we search backward, then those nodes chained together by prev_node_on_shortest_path
        // will form a path that's a reverse of path get by forward search
        Node prev_node_on_shortest_path;
        public double shortest_cost_from_src;
        double cost_to_prev_node_on_path;

        /**Constructor for the DijkstraNodeInfo object */
        DijkstraNodeInfo() {
            clear();
        }

        /**
         * reset info to prepare for next run of Dijkstra with a different source node
         */
        public void clear() {
            path_found = false;
            shortest_cost_from_src = Double.POSITIVE_INFINITY;
            prev_node_on_shortest_path = null;
            cost_to_prev_node_on_path = Double.POSITIVE_INFINITY;
        }
    }
}

package com.gaocan.publictransportation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jpenguin.graph.DijkstraGraph;
import org.jpenguin.graph.Node;
import org.jpenguin.graph.Path;
import org.jpenguin.graph.UnknownNodeExcepton;
import org.jpenguin.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Given a schedule and criteria return optimal paths between two
 * AreaServedByStationss
 */
public class RoutePlanner implements IRoutePlanner {
    private static Logger logger = LoggerFactory.getLogger(RoutePlanner.class);

    public static final int DEFAULT_PATHS_FOR_EACH_LINE = 12;

    /**
     * starting from one lsp, num of dest nodes to reach before terminating the
     * search
     */
    private int pathsToGetForEachLine = DEFAULT_PATHS_FOR_EACH_LINE;

    /** max path cost allowed in relation to best path so far */
    public static final double searchEndCostToBestRatio = 10.0;
    private Schedule schedule;

    /** planning graph where each node is a LSP */
    private DijkstraGraph lspGraph;
    private boolean keep_going;
    // for pair and A* search
    boolean a_star_precomputed_costs_read = false;

    /** area of biggest areas whose paths are precomputed */
    Vector biggestAreas = new Vector();

    /**
     * if an area is among the biggest ones whose paths are precomputed, this is
     * the index of the area within the array of the biggest areas
     */
    int srcBigAreaIndex;
    int destBigAreaIndex;

    /** exclude certain lsps from being path ends */
    private SrcDestLspConstraint srcDestLspConstraint = null;

    /**
     * for those top areas whose pairwise paths have been precomputed, the
     * starting lsps' line numbers, array indexes are top area's index above
     */
    Vector[][] precomputedPathStartingLinesForBigAreas;
    boolean srcAreaIsBig = false;
    boolean destAreaIsBig = false;

    /** If true then big areas' paths are precomputed */
    boolean usePrecompute = false;

    /**
     * If false then not only the paths between pairs of big areas, but also the
     * paths from bigs areas to every station are precomputed
     */
    boolean onlyAreaServedByStationsPairPrecomputed = false;

    /** a file listing the names of the biggest areas */
    String biggestAreaFileName;

    /** a file listing the starting lines of the paths between the biggest areas */
    String precomputedBigAreaPairPathsFileName;

    /**
     * a file listing the costs of the paths from the biggest areas to every
     * station
     */
    String precomputedFromBigAreaPathCostsFileName;

    /**
     * a file listing the costs of the paths from every station to the biggest
     * areas
     */
    String precomputedToBigAreaPathCostsFileName;

    // ------------------------ Configuration parameters
    // ------------------------------------------------
    private boolean initialized = false;

    /**
     * no Astar, for precomputing astar results
     * 
     * @param sched
     */
    public RoutePlanner(Schedule sched) {
        this(sched, null, null, null, null, false, false);
    }

    /**
     * 
     * @param sched
     * @param bigAreaFileName
     * @param fromBigAreaPathCostFile
     * @param toBigAreaPathCostFile
     * @param pairwiseBigAreaPathFile
     * @param perm_cache_size
     * @param _usePrecompute
     * @param _onlyAreaServedByStationsPairPrecomputed
     * @throws java.io.FileNotFoundException
     */
    public RoutePlanner(Schedule sched, String bigAreaFileName, String fromBigAreaPathCostFile, String toBigAreaPathCostFile,
            String pairwiseBigAreaPathFile, boolean _usePrecompute, boolean _onlyAreaServedByStationsPairPrecomputed) {
        schedule = sched;
        usePrecompute = _usePrecompute;
        onlyAreaServedByStationsPairPrecomputed = _onlyAreaServedByStationsPairPrecomputed;
        biggestAreaFileName = bigAreaFileName;

        precomputedFromBigAreaPathCostsFileName = fromBigAreaPathCostFile;
        precomputedToBigAreaPathCostsFileName = toBigAreaPathCostFile;
        precomputedBigAreaPairPathsFileName = pairwiseBigAreaPathFile;
    }

    /**
     * build the graph and read precomputed results
     * 
     */
    public synchronized void init() {
        // multiple JSPs may try calling init() at same time but queued up
        if (isInitialized()) {
            return;
        }
        // build the graph
        buildPlanningGraph();
        if (usePrecompute) {
            try {
                initAStarData();
            } catch (Exception e) {
                // abandon astar if unable to read
                logger.error("AStar", e);
                usePrecompute = false;
            }
        }
        setInitialized(true);
    }

    /**
     * enable A* search, this is for A star (precompute min costs between top
     * areas and ALL stations)
     */
    void initAStarData() throws Exception {
        long begin = System.currentTimeMillis();

        // read list of biggest areas
        StreamTokenizer tokenizer = new StreamTokenizer(new InputStreamReader(new FileInputStream(biggestAreaFileName), "utf-8"));
        tokenizer.whitespaceChars(',', ',');
        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            AreaServedByStations area = schedule.getAreaServedByStationsByName(tokenizer.sval);
            if (area == null) {
                throw new IllegalStateException("Big AreaServedByStations not known in schedule: " + tokenizer.sval);
            }
            biggestAreas.add(area);
        }

        // read precompute
        readPrecomputedResults(precomputedFromBigAreaPathCostsFileName, precomputedToBigAreaPathCostsFileName,
                precomputedBigAreaPairPathsFileName, biggestAreas, onlyAreaServedByStationsPairPrecomputed);
        long end = System.currentTimeMillis();
        Logging.info(this, "init", "Loading schedule and precomputed results took " + (end - begin) + " ms");
    }

    /**
     * 
     * @param areaName
     * @return -1 if area is not big
     */
    int getBigAreaIndex(String areaName) {
        for (int i = 0; i < biggestAreas.size(); i++) {
            AreaServedByStations area = (AreaServedByStations) biggestAreas.get(i);

            if (area.getName().equals(areaName)) {
                return i;
            }
        }

        return -1;
    }

    public List<Path> getShortestPaths(String srcAreaName, String destAreaName) throws NoSuchStationException {
        AreaServedByStations srcArea = this.schedule.getAreaServedByStationsByName(srcAreaName);
        if (srcArea == null) {
            throw new NoSuchStationException(srcAreaName);
        }
        AreaServedByStations destArea = this.schedule.getAreaServedByStationsByName(destAreaName);
        if (destArea == null) {
            throw new NoSuchStationException(destAreaName);
        }
        return getShortestPaths(srcArea, destArea);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gaocan.publictransportation.IRoutePlanner#getShortestPaths(com.gaocan
     * .gis.AreaServedByStations, com.gaocan.gis.AreaServedByStations)
     */
    private synchronized ArrayList<Path> getShortestPaths(AreaServedByStations srcArea, AreaServedByStations destArea)
            throws NoSuchStationException {
        double best_path_cost_so_far;

        logger.debug("getShortestPaths", "src:" + srcArea.getName() + ", dest:" + destArea.getName());

        if (!isInitialized()) {
            throw new java.lang.IllegalStateException("Planner has not been initialized");
        }

        if ((srcArea == null) || (destArea == null)) {
            throw new NoSuchStationException("null src or dest area");
        }

        keep_going = true;

        /*
         * We calc shortest paths for each pair of <src_station, any line> and
         * <dest_station, any line> and list the top N shortest paths.
         */
        ArrayList<Path> candidatePaths = new ArrayList<Path>();

        boolean a_star_mode = false;
        boolean a_star_src = false;
        int a_star_top_area_index = 0;

        try {
            int src_lines = srcArea.getNumLinesPassing();
            int dest_lines = destArea.getNumLinesPassing();

            // save time by start from the smaller station (less train lines
            // passing)
            boolean forward_search = (src_lines < dest_lines);

            getLspGraph().setPathSearchDirection(forward_search);

            // check if src area is one of biggeste areas with precomputed path
            // cost
            // to every other station, if yes use A* search mode
            // also check if both src and dest areas are one of the biggest
            // ones,
            // then their paths are precomputed with start lsp line nubmers
            // limited
            boolean srcAreaIsBig = false;
            boolean destAreaIsBig = false;

            srcBigAreaIndex = getBigAreaIndex(srcArea.getName());

            if (srcBigAreaIndex != -1) {
                srcAreaIsBig = true;

                // src area is both a big ciy and has more lines than dest area
                if (!forward_search && a_star_precomputed_costs_read) {
                    a_star_mode = true;
                    a_star_top_area_index = srcBigAreaIndex;
                    a_star_src = true;
                }
            }

            // check if dest area if big
            destBigAreaIndex = getBigAreaIndex(destArea.getName());

            if (destBigAreaIndex != -1) {
                destAreaIsBig = true;

                if (forward_search && a_star_precomputed_costs_read) {
                    // dest area is bigger
                    a_star_mode = true;
                    a_star_top_area_index = destBigAreaIndex;
                    a_star_src = false;
                }
            }

            // exclude all lsps in the same-area as the start node.
            // during searches, do not let path loop back to these lsps any
            // more.
            // this is because a loop back path can be reduced to a non-loop
            // path
            // (removing stations prior to the loop) while still is a valid path
            getLspGraph().clearNodeExclusionPredicate();
            getLspGraph().setNodeExclusionPredicate(new IsLspInArea(forward_search ? srcArea : destArea));

            // do not use max path cost limit at first
            best_path_cost_so_far = Double.POSITIVE_INFINITY;

            if (forward_search) {
                // stop search once enough dest got
                getLspGraph().setTerminateCondition(new IsLspInArea(destArea), pathsToGetForEachLine);
            } else {
                getLspGraph().setTerminateCondition(new IsLspInArea(srcArea), pathsToGetForEachLine);
            }

            // search starts from src station's outgoing lines
            Iterator<LineStationPair> startLspIt = (forward_search ? srcArea.getLineStationIterator() : destArea
                    .getLineStationIterator());

            // start_lsp means from where to start search, it can be src station
            // or dest station
            while (startLspIt.hasNext()) {
                // check if client has told us to abort search
                if (keep_going == false) {
                    return candidatePaths;
                }

                getLspGraph().setCostThreshold(searchEndCostToBestRatio * best_path_cost_so_far);

                LineStationPair start_lsp = startLspIt.next();
                logger.debug("try start from lsp " + start_lsp);

                // time constraint ?
                if (getSrcDestLspConstraint() != null) {
                    // exclude start lsps that do not meet constraints, these
                    // start lsps can be either
                    // depart or arrival lsp depend on serach direction
                    if (!forward_search && !getSrcDestLspConstraint().isValidArrivalLsp(start_lsp)) {
                        continue;
                    } else if (forward_search && !getSrcDestLspConstraint().isValidDepartLsp(start_lsp)) {
                        continue;
                    }
                } else if (srcAreaIsBig && destAreaIsBig && precomputedPathStartingLinesForBigAreas != null) {
                    // both src and dest are top areas, check precomputed paths
                    // only do this if no time constraint, b/c the
                    // precomputation is done w/o constraints but w. pruning
                    // so the precomputed result may not include paths that
                    // would have been included
                    // if there's constraint
                    int k;

                    // check if start_lsp is start lsp of one of the paths
                    for (k = 0; k < precomputedPathStartingLinesForBigAreas[srcBigAreaIndex][destBigAreaIndex].size(); k++) {
                        String precomputed_line = (String) precomputedPathStartingLinesForBigAreas[srcBigAreaIndex][destBigAreaIndex]
                                .get(k);
                        if (start_lsp.getLine().matchesKey(precomputed_line)) {
                            break;
                        }
                    }

                    // no, then skip this start lsp
                    if (k == precomputedPathStartingLinesForBigAreas[srcBigAreaIndex][destBigAreaIndex].size()) {
                        continue;
                    }
                }

                // set starting graph node
                getLspGraph().setStartNode(start_lsp);

                // A* search ?
                if (a_star_mode) {
                    getLspGraph().setAStarSearchMode(a_star_src, a_star_top_area_index);
                }

                getLspGraph().computeShortestPaths();

                // collect shortest paths
                Vector final_nodes = getLspGraph().getFinalNodesReached();
                logger.debug(" initial final nodes " + final_nodes);

                // It's not possible to reach dest area from this src station
                if (final_nodes == null) {
                    continue;
                }

                //
                for (int path_no = 0; path_no < final_nodes.size(); path_no++) {
                    if (getSrcDestLspConstraint() != null) {
                        // exclude end lsps that do not meet constraints, these
                        // end lsp can be either
                        // depart or arrival lsp depend on serach direction
                        LineStationPair lsp = (LineStationPair) ((Node) final_nodes.get(path_no)).getNodeData();
                        if (forward_search && !getSrcDestLspConstraint().isValidArrivalLsp(lsp)) {
                            continue;
                        } else if (!forward_search && !getSrcDestLspConstraint().isValidDepartLsp(lsp)) {
                            continue;
                        }
                    }

                    Path candidate_path = getLspGraph().getShortestPath((Node) final_nodes.get(path_no));
                    if (candidate_path.getCost() < best_path_cost_so_far) {
                        best_path_cost_so_far = candidate_path.getCost();
                    }
                    if (candidate_path.getLength() > 0)
                        candidatePaths.add(candidate_path);
                }
            }

        } catch (UnknownNodeExcepton e) {
            e.printStackTrace();
        }
        logger.debug("getShortestPaths", "found " + candidatePaths.size() + " paths");

        return candidatePaths;
    }

    /**
     * build a big ST graph, where each node is a pair <station,line>, there's
     * an edge from <s1, l> to <s2,l> iff s1 and s2 are two consecutive stations
     * on l, also an edge between <s, l1> and <s, l2> for transfer between two
     * lines at s.
     * 
     * So now from ShenZhen to Beijing becomes a single source all dest shortest
     * path problem. Starting from all <ShenZhen, lineX> for lineX , calc all
     * dest shortest paths. Whenever a Beijing is reached, then found.
     * 
     * if source lsps too many, we compute from dest backwards
     */
    public void buildPlanningGraph() {
        LineStationPair lsp1;
        LineStationPair lsp2;
        Iterator<LineStationPair> lspIt1;
        Iterator<LineStationPair> lspIt2;
        LineStationPair prev_lsp;
        Node node1;
        Node node2;
        Node prev_node;

        Logging.info(this, "buildPlanningGraph", "Start building graph for schedule " + schedule.getName());

        setLspGraph(new DijkstraGraph());

        // same line: there's an edge from <s1, l> to <s2,l> iff s1 and s2 are
        // two consecutive stations on l
        for (Line line : schedule.getLines()) {
            boolean biDir = line.isBidirectional();
            prev_lsp = null;
            prev_node = null;
            lspIt1 = line.getLspIterator();
            while (lspIt1.hasNext()) {
                lsp1 = (LineStationPair) lspIt1.next();
                // it's possible for bidrectional lines to have two identical
                // stations in the same line
                if ((node1 = getLspGraph().getNode(lsp1)) == null) {
                    node1 = getLspGraph().addNode(lsp1);
                }
                if (prev_lsp != null) {
                    try {
                        double cost = prev_lsp.getCostToNextLspOnPath(lsp1);
                        // it's possible for bidrectional lines to have two
                        // identical stations in the same line
                        // make sure we do not add edge twice for these stations
                        if (node1.edgeFromNode(prev_node) == null) {
                            getLspGraph().addEdge(prev_node, node1, cost);
                        }
                        // bidrectional line?
                        if (biDir) {
                            // it's possible for bidrectional lines to have two
                            // identical stations in the same line
                            // make sure we do not add edge twice for these
                            // stations
                            if (node1.edgeToNode(prev_node) == null) {
                                getLspGraph().addEdge(node1, prev_node, cost);
                            }
                        }
                    } catch (Exception e) {
                        // most likely a lsp is too late for being a transfer
                        // for another lsp, ignore
                        logger.error("build graph", e);
                    }
                }
                prev_lsp = lsp1;
                prev_node = node1;
            }
        }

        // edge between <s1, l1> and <s2, l2>, if s1 and s2 are either the same
        // station or
        // two stations in the same area, and l1 and l2 are not just reverse
        // directions of the same line (like train lines K121 and K122,
        // or bus lines 11 but opposite direction)
        // for each area
        for (AreaServedByStations area : schedule.getAreas()) {
            // for each line in that area
            lspIt1 = area.getLineStationIterator();
            while (lspIt1.hasNext()) {
                lsp1 = (LineStationPair) lspIt1.next();
                node1 = getLspGraph().getNode(lsp1);
                // for each other station (including this station) in the
                // same area and all the lines pass that station
                lspIt2 = area.getLineStationIterator();
                while (lspIt2.hasNext()) {
                    lsp2 = (LineStationPair) lspIt2.next();
                    // same line and same station? skip
                    if (lsp1 == lsp2) {
                        continue;
                    }

                    // not on the same line but just travelling at opposite
                    // directions? skip
                    if ((lsp1.getLine().getBeginStation() == lsp2.getLine().getEndStation())
                            && (lsp2.getLine().getBeginStation() == lsp1.getLine().getEndStation())) {
                        // System.out.println ("Skipping opposite trains" +
                        // lsp.line + lsp2.line);
                        continue;
                    }

                    // find node for lsp2
                    node2 = getLspGraph().getNode(lsp2);
                    // add transfer edge
                    try {
                        getLspGraph().addEdge(node1, node2, lsp1.getCostToNextLspOnPath(lsp2));
                    } catch (Exception ex) {
                        // do nothing as getCostToNextLspOnPath() can throw if
                        // lsp1/2 more than 12 hours apart
                        ;
                    }
                }
            }
        }

        // LOG
        Logging.info(this, "buildPlanningGraph",
                "Done building graph, " + schedule.getAreas().size() + " areas, " + schedule.getNumStations() + " stations, "
                        + getLspGraph().getNumberNodes() + " nodes");
    }

    /**
     * Initialize every LSP (that is every node in the graph)'s numTopNNodes
     * field to the number of biggest areas and allocate the top N costs array
     * 
     * @param n_top_nodes
     * @see
     */
    void allocateTopNNodes(int n_top_nodes) {
        Iterator lspIt;
        LineStationPair lsp;
        Node node;

        // ### rewrite by step through nodes in graph directly?
        for (Station st : schedule.getStations()) {
            lspIt = (st).getLineStationIterator();

            while (lspIt.hasNext()) {
                lsp = (LineStationPair) lspIt.next();
                node = getLspGraph().getNode(lsp);
                if (node == null) {
                    logger.warn( "setNumTopNNodes", "Due to error in orig sched data, " + lsp + " is dropped");
                } else {
                    node.setNumTopNNodes(n_top_nodes);
                }
            }
        }
    }

    void readPrecomputedMinCostFile(String filename, boolean from_top) throws IOException {
        InputStreamReader reader;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filename);
            reader = new InputStreamReader(fis, "utf-8");

            StreamTokenizer tokenizer = new StreamTokenizer(reader);

            // ~~~ Do not need this, it will set all chars to ordinary
            /*
             * tokenizer.resetSyntax();
             */
            tokenizer.eolIsSignificant(false);

            Collection<Node> nodeColl = getLspGraph().getNodes();
            // must sort the nodes (LSPs) and write their astar costs in that
            // order to the file,
            // in order to reconstruct the astar costs to the correct nodes. the
            // default iterator
            // order is not the same between astar run and real web server run
            Node[] nodes = new Node[0];
            nodes = nodeColl.toArray(nodes);
            Arrays.sort(nodes, new Comparator<Node>() {
                public int compare(Node o1, Node o2) {
                    LineStationPair lsp1 = (LineStationPair) o1.getNodeData();
                    LineStationPair lsp2 = (LineStationPair) o2.getNodeData();
                    if (lsp1 instanceof Comparable == false || lsp2 instanceof Comparable == false) {
                        throw new IllegalStateException("LSPs must be sortable for write and read back astar costs");
                    }
                    return ((Comparable) lsp1).compareTo((Comparable) lsp2);
                }
            });

            for (int bigAreaNo = 0; bigAreaNo < biggestAreas.size(); bigAreaNo++) {
                if (bigAreaNo % 10 == 0 || bigAreaNo == biggestAreas.size() - 1) {
                    Logging.info(this, "", " Loading  min cost " + (from_top ? "from" : "to") + " big area " + bigAreaNo);
                }
                for (Node node : nodes) {
                    int c = tokenizer.nextToken();

                    switch (c) {
                    case StreamTokenizer.TT_EOF:
                        logger.warn( "", "Seen unexpected EOF");
                        return;
                    case StreamTokenizer.TT_WORD:
                        if (tokenizer.sval.equalsIgnoreCase("Failed")) {
                            break;
                        }
                    case StreamTokenizer.TT_NUMBER:
                        double cost = tokenizer.nval;
                        if (cost < 0) {
                            logger.error( "", " Seen negative astar cost");
                            cost = 0;
                        }

                        // Here the cost is min graph cost
                        if (from_top) {
                            node.setMinCostFromTopNode(bigAreaNo, cost);
                        } else {
                            node.setMinCostToTopNode(bigAreaNo, cost);
                        }
                        break;
                    }

                    // next is node hash code for checksum
                    if (tokenizer.nextToken() != StreamTokenizer.TT_NUMBER)
                        throw new IllegalStateException("Missing lsp checksum");
                    int lspVal = (int) tokenizer.nval;
                    if (lspVal != node.hashCode()) {
                        logger.error( "node lsp checksum error", node.toString());
                    }
                }
            }

            // for top area
            if (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                logger.error( "", " expected EOF not seen, extra LSPs in astar file");
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    /**
     * Method declaration
     * 
     * @param min_from_cost_filename
     * @param min_to_cost_filename
     * @param pair_path_filename
     * @param biggestAreas
     *            Description of the Parameter
     * @param top_area_pair_only
     *            Description of the Parameter
     * @see
     */
    public void readPrecomputedResults(String min_from_cost_filename, String min_to_cost_filename, String pair_path_filename,
            Vector biggestAreas, boolean top_area_pair_only) throws IOException {
        int n_top_areas = biggestAreas.size();
        // must do this before others
        allocateTopNNodes(n_top_areas);

        Logging.info(this, "", "Reading pair precomputed result");
        readPrecomputedAreaServedByStationsPairPaths(pair_path_filename, biggestAreas);
        Logging.info(this, "", "Done reading pair precomputed result");
        //
        if (!top_area_pair_only) {
            readPrecomputedMinCostFile(min_from_cost_filename, true);
            readPrecomputedMinCostFile(min_to_cost_filename, false);
            a_star_precomputed_costs_read = true;
        }
    }

    /**
     * Method declaration
     * 
     * @param filename
     * @param top_areas
     *            Description of the Parameter
     * @see
     */
    void readPrecomputedAreaServedByStationsPairPaths(String filename, Vector top_areas) {
        int c;
        InputStreamReader reader;
        int srcBigAreaIndex = -1;
        int destBigAreaIndex = 0;
        String srcArea_name = new String("none");
        String destArea_name = new String("none");

        int n_top_areas = top_areas.size();

        int n_lines_text_read = 1;

        try {
            reader = new InputStreamReader(new FileInputStream(filename), "utf-8");

            StreamTokenizer tokenizer = new StreamTokenizer(reader);

            tokenizer.ordinaryChars('0', '9');

            // for clear all the bits
            tokenizer.wordChars('0', '9');
            // for T43/T46 as one word instead of two
            tokenizer.wordChars('/', '/');
            tokenizer.ordinaryChar('-');
            tokenizer.ordinaryChar(':');

            precomputedPathStartingLinesForBigAreas = new Vector[n_top_areas][];

            for (int i = 0; i < n_top_areas; i++) {
                precomputedPathStartingLinesForBigAreas[i] = new Vector[n_top_areas];
                for (int j = 0; j < n_top_areas; j++) {
                    precomputedPathStartingLinesForBigAreas[i][j] = new Vector();
                }
            }

            // time
            tokenizer.eolIsSignificant(true);

            while (true) {
                c = tokenizer.nextToken();

                switch (c) {
                case StreamTokenizer.TT_EOL:
                    n_lines_text_read++;
                    break;
                case StreamTokenizer.TT_EOF:
                    return;
                case StreamTokenizer.TT_WORD:
                    // no route between two areas
                    if (tokenizer.sval.equalsIgnoreCase("Failed")) {
                        // skip this line and go to next area
                        while (true) {
                            c = tokenizer.nextToken();

                            if ((c == StreamTokenizer.TT_EOL) || (c == StreamTokenizer.TT_EOF)) {
                                break;
                            }
                        }
                    }
                    // area name (Chinese char)?
                    else if (tokenizer.sval.charAt(0) > 127) {
                        // src area?
                        int cn;
                        srcArea_name = tokenizer.sval;
                        for (cn = 0; cn < n_top_areas; cn++) {
                            AreaServedByStations area = (AreaServedByStations) top_areas.get(cn);
                            if (area.getName().equals(srcArea_name)) {
                                srcBigAreaIndex = cn;
                                break;
                            }
                        }

                        if (cn == n_top_areas) {
                            throw new InvalidScheduleDataException("", "got unknown src :" + tokenizer.sval);
                        }

                        // get dest area
                        c = tokenizer.nextToken();

                        if (c != '-') {
                            throw new InvalidScheduleDataException("", "Expecting - ");
                        }

                        // dest area
                        c = tokenizer.nextToken();
                        destArea_name = tokenizer.sval;

                        for (cn = 0; cn < n_top_areas; cn++) {
                            AreaServedByStations area = (AreaServedByStations) top_areas.get(cn);

                            if (area.getName().equals(destArea_name)) {
                                destBigAreaIndex = cn;

                                break;
                            }
                        }

                        if (cn == n_top_areas) {
                            throw new InvalidScheduleDataException("", "After -, got unknown dest :" + tokenizer.sval);
                        }

                        c = tokenizer.nextToken();

                        if (c != ':') {
                            throw new InvalidScheduleDataException("", "Expecting : ");
                        }
                    }
                    // train line number
                    else {
                        boolean found = false;

                        // if not already inside
                        for (int line_no = 0; line_no < precomputedPathStartingLinesForBigAreas[srcBigAreaIndex][destBigAreaIndex]
                                .size(); line_no++) {
                            if (precomputedPathStartingLinesForBigAreas[srcBigAreaIndex][destBigAreaIndex].get(line_no).equals(
                                    tokenizer.sval)) {
                                found = true;

                                break;
                            }
                        }

                        if (!found) {
                            precomputedPathStartingLinesForBigAreas[srcBigAreaIndex][destBigAreaIndex].add(tokenizer.sval);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("At line " + n_lines_text_read);
            // null precomputedPathStartingLinesForBigAreas
            precomputedPathStartingLinesForBigAreas = null;
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gaocan.publictransportation.IRoutePlanner#setSchedule(com.gaocan.
     * publictransportation.Schedule)
     */
    public void setSchedule(Schedule sched) {
        schedule = sched;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gaocan.publictransportation.IRoutePlanner#getSchedule()
     */
    public Schedule getSchedule() {
        return schedule;
    }

    public void setLspGraph(DijkstraGraph _lspGraph) {
        lspGraph = _lspGraph;
    }

    public DijkstraGraph getLspGraph() {
        return lspGraph;
    }

    public void stopSearch() {
        keep_going = false;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gaocan.publictransportation.IRoutePlanner#setSrcDestLspConstraint
     * (com.gaocan.publictransportation.SrcDestLspConstraint)
     */
    public void setSrcDestLspConstraint(SrcDestLspConstraint srcDestLspConstraint) {
        this.srcDestLspConstraint = srcDestLspConstraint;
    }

    public SrcDestLspConstraint getSrcDestLspConstraint() {
        return srcDestLspConstraint;
    }

    public int getPathsToGetForEachLine() {
        return pathsToGetForEachLine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gaocan.publictransportation.IRoutePlanner#setPathsToGetForEachLine
     * (int)
     */
    public void setPathsToGetForEachLine(int pathsToGetForEachLine) {
        this.pathsToGetForEachLine = pathsToGetForEachLine;
    }
}

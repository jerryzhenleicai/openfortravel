package org.jpenguin.graph;


public class ShortestPathNotComputedExcepton extends Exception {
    ShortestPathNotComputedExcepton() {
        super("To ask path for a denode, it must have been computed");
    }
}

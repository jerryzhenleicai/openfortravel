package org.jpenguin.graph;

/**
 * 
 * @author zcai
 *
 */
public interface UnaryPredicate {
    /**
     * This condition is true on object 
     * @param obj 
     * @return 
     */
    boolean isTrueOn(Object obj);
}

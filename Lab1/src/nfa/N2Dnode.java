package nfa;

import java.util.NavigableSet;

class N2Dnode {
    private boolean exists = false;
    private final int subtreeCapacity;
    private boolean subtreeInitiated = false;
    N2Dnode[] subtree;

    N2Dnode(int capacity) {
        subtreeCapacity = capacity;
    }

    void add(NavigableSet<Integer> dfaNodeClass) {
        if (dfaNodeClass.isEmpty()) exists = true;
        else {
            if (!subtreeInitiated) initiateSubtree();
            Integer key1 = dfaNodeClass.first();
            if ( subtree[key1]==null ) subtree[key1] = new N2Dnode(subtreeCapacity);
            subtree[key1].add(dfaNodeClass.tailSet(key1, false));  // false -> not inclusive
        }
    }

    boolean has(NavigableSet<Integer> dfaNodeClass) {
        if (dfaNodeClass.isEmpty()) return exists;
        else {
            if (!subtreeInitiated) return false;
            Integer key1 = dfaNodeClass.first();
            if ( subtree[key1]==null ) return false;
            else return subtree[key1].has(dfaNodeClass.tailSet(key1, false));  // false -> not inclusive
        }
    }

    private void initiateSubtree() {
        subtree = new N2Dnode[subtreeCapacity];
        subtreeInitiated = true;
    }
}

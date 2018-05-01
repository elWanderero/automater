package DFAgraph;

import nfa.EquivalenceClass;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;

public class DFA {
    public DFAnode start;
    public DFAnode deathNode;
    private final Character[] alphabet;
    private int size;

    public DFA(Character[] alphabet) {
        this.alphabet = alphabet;
        size = 0;
        deathNode = new DFAnode(alphabet, newId());
    }

    public DFAnode makeNode() {
        return new DFAnode(alphabet, newId());
    }

    public void minimise() {
        Map<DFAnode, Integer> classTable = new HashMap<>(size);
    }

    @Override
    public String toString() {
        if ( start == null ) return "[Empty DFA]";
        StringBuilder str = new StringBuilder();
        Queue<DFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueued = new boolean[size];
        queue.add(start);
        alreadyQueued[start.id] = true;
        while ( !queue.isEmpty() ) {
            DFAnode node = queue.remove();
            node.addToStringBuilder(str);
            for ( DFAnode edge : node.edges.values() ) {
                if ( !alreadyQueued[edge.id] ) {
                    queue.add(edge);
                    alreadyQueued[edge.id] = true;
                }
            }
        }
        return str.toString();
    }

    private int newId() { return size++; }

}

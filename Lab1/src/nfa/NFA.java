/* Implemented as a function table, a variable denoting the start state, and a list of the accepting states.
 *
 */

package nfa;

import DFAgraph.DFA;
import DFAgraph.DFAnode;

import java.util.*;

public class NFA {
    public final boolean[] acceptingStates;
    public final Integer start;
    public final Map<Character, EquivalenceClass>[] transitionFcn;
    public final Character[] alphabet;
    public final int sizeBound;

    public NFA(Character[] alphabet,
               Map<Character, EquivalenceClass>[] fcnTable,
               Integer start,
               boolean[] acceptingStates) {
        this.acceptingStates = acceptingStates;
        this.start = start;
        this.transitionFcn = fcnTable;
        this.alphabet = alphabet;
        sizeBound = fcnTable.length;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i=0 ; i<transitionFcn.length ; i++) {
            if ( !(transitionFcn[i]==null) ) {
                String annotation = acceptingStates[i] ? "*" : "";
                if ( i==start ) annotation += "•";
                str.append(String.format("%2d%-2s||", i, annotation));
                for (Character c : alphabet) {
                    if (transitionFcn[i].get(c).size() > 0) {
                        str.append(String.format(" %c", c));
                        for (Integer id : transitionFcn[i].get(c)) str.append(String.format("→%2d", id));
                        str.append(" |");
                    }
                }
                str.append(System.lineSeparator());
            }
        }
        return str.toString();
    }

    public DFA toDFA() {
        DFA dfa = new DFA(alphabet, true);
        DFAnode startNode = dfa.makeNode();
        dfa.setStart(startNode);
        
        EquivalenceClass startClass = new EquivalenceClass();
        startClass.add(start);

        Queue<EquivalenceClass> queue = new LinkedList<>();
        queue.add( startClass );

        TreeMap<EquivalenceClass, DFAnode> alreadyQueued = new TreeMap<>();
        alreadyQueued.put(new EquivalenceClass(), dfa.getDeathNode());  // I'm very clever.
        alreadyQueued.put(startClass, startNode);

        while (!queue.isEmpty()) {
            EquivalenceClass equivNode = queue.remove();
            DFAnode dfaNode = alreadyQueued.get(equivNode);
            for (Integer id : equivNode) {
                if (acceptingStates[id]) {
                    dfaNode.accepting = true;
                    break;
                }
            }

            for (Character c : alphabet) {
                EquivalenceClass equivEdge = new EquivalenceClass();
                DFAnode dfaEdge;
                for (Integer nfaNode : equivNode) {
                    EquivalenceClass partialEdge = transitionFcn[nfaNode].get(c);
                    equivEdge.addAll(partialEdge);
                }

                if ( alreadyQueued.containsKey(equivEdge) ) dfaEdge = alreadyQueued.get(equivEdge);
                else {
                    queue.add(equivEdge);
                    dfaEdge = dfa.makeNode();
                    alreadyQueued.put(equivEdge, dfaEdge);
                }
                dfaNode.edges.put(c, dfaEdge);
            }
        }
        return dfa;
    }
}

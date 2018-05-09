package nfa;

import DFAgraph.DFA;
import DFAgraph.DFAnode;
import eNFAgraph.ENFA;

import java.util.*;

/**
 * A function table, a variable denoting the start state, and a list of the accepting states.
 * The NFA is implemented a bit different than the other automata. It has no node class,
 * but instead just uses a table, in the form of a Map<>-array, for the transition function.
 * This is because you can go directly from an epsilon-NFA to an equivalent DFA, by keeping
 * track of epsilon-closures and subset construction classes at the same time. But when
 * doing that I realised that to avoid computing the subset construction class multiple
 * time I could have a table keeping track of them. And then i realised that that table
 * was in essence the intermediary equivalent NFA. So I might as well make a proper class
 * out of it and take advantage of it. It separates the construction of the two equivalence
 * classes (epsilon closures and subset construction classes) into two phases, which makes
 * it easier for me to reason about.
 */
public class NFA {
    public final boolean[] acceptingStates;
    public final Integer startNode;
    public final Map<Character, EquivalenceClass>[] transitionFcn;
    public final Character[] alphabet;
    public final int sizeBound;

    // The core of the NFA, the transition function, must be constructed externally and
    // supplied to this constructor. It is then just bound to the local variable.
    public NFA(Character[] alphabet,
               Map<Character, EquivalenceClass>[] transitionTable,
               Integer start,
               boolean[] acceptingStates) {
        this.acceptingStates = acceptingStates;
        this.startNode = start;
        this.transitionFcn = transitionTable;
        this.alphabet = alphabet;
        sizeBound = transitionTable.length;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i=0 ; i<transitionFcn.length ; i++) {
            if ( !(transitionFcn[i]==null) ) {
                String annotation = acceptingStates[i] ? "*" : "";
                if ( i== startNode) annotation += "•";
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

    // Return a Graphviz .gv-formatted string, ready for dot.
    public String toGVstring() {
        List<Integer> acceptingNodes = new LinkedList<>();
        for (int i=0 ; i<acceptingStates.length ; ++i) {
            if (acceptingStates[i]) acceptingNodes.add(i);
        }
        StringBuilder str = ENFA.gvPrefix(startNode, acceptingNodes);
        for (int i=0 ; i<transitionFcn.length ; i++) {
            String prefix = String.valueOf(i) + " -> ";
            if ( !(transitionFcn[i]==null) ) for (Character c : alphabet) {
                String escapedChar = c == '\\' || c == '\"' ? "\\" + c.toString() : c.toString();
                String suffix = " [ label = \"" + escapedChar + "\" ];" + System.lineSeparator();
                for (Integer edgeId : transitionFcn[i].get(c)) {
                    str.append(prefix);
                    str.append(edgeId);
                    str.append(suffix);
                }
            }
        }
        str.append("}");
        return str.toString();
    }

    /* We turn the NFA into a deterministic finite automaton using lazy evaluation, starting from
     * the start node. The nodes of the DFA are sets of NFA-nodes, and these are intermediately
     * kept track of using EquivalenceClass, before being turned into DFAnodes in the end (Note
     * that they are not technically equivalence classes, but whatever.) This is so that we
     * separate and identify these sets in the proper way, even if we should construct the same
     * set multiple times. The EquivalenceClass is actually a Set of Integers.
     *
     * Other than that, it is the usual loop-through-a-graph-without-repetition technique. I call
     * it the queue-and-checklist technique. We start from the start node, which is its own
     * equivalence class, and construct the equivalence classes its edges lead to. These are then
     * put in a queue. The classes in this queue are evaluated in the same way, and the process
     * iterates until the queue is empty. To make sure that we don't evaluate the same element
     * twice (reaching it several times via different paths) we keep a checklist of all equivalence
     * classes that have already been queued once. Since the equivalence classes are subsets, this
     * checklist is implemented as a lexicographically ordered Map (TreeMap, but the lexicographic
     * ordering capabilities come from the implementation of EquivalenceClass.)
     */
    public DFA toDFA() {
        DFA dfa = new DFA(alphabet, true);
        DFAnode startNode = dfa.makeNode();
        dfa.setStart(startNode);
        
        EquivalenceClass startClass = new EquivalenceClass();
        startClass.add(this.startNode);

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

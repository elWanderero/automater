package eNFAgraph;

import java.util.*;

/**
 * Epsilon-non-deterministic automaton node (i.e. state) class. Because of the
 * special structure e-NFA's have in this project, they have a bunch of epsilon-
 * edges implemented as a list of other nodes, but only a maximum of one lettered
 * edge. This edge is expected to be labelled with exactly one letter or the
 * entire alphabet, otherwise things may screw up elsewhere.
 */
public class ENFAnode {
    public boolean accepting = false;
    public ENFAnode edge;
    public final Character[] edgeLetters;
    public boolean hasLetterEdge;
    public ArrayList<ENFAnode> emptyEdges = new ArrayList<>();

    // For nfa.NFA creation
    public int id;

    // Construct node without edges
    ENFAnode(int id) {
        this.id = id;
        hasLetterEdge = false;
        edgeLetters = null;
    }
    // Construct node with one lettered edge
    ENFAnode(int id, ENFAnode edge, Character[] edgeLetters) {
        this.id = id;
        hasLetterEdge = true;
        this.edge = edge;
        this.edgeLetters = edgeLetters;
    }
    // Construct node with one epsilon-edge
    ENFAnode(int id, ENFAnode epsilonEdge) {
        this.id = id;
        hasLetterEdge = false;
        emptyEdges.add(epsilonEdge);
        edgeLetters = null;
    }

    void toStringBuilder(boolean[] alreadyQueuedNodes,
                           LinkedList<ENFAnode> queue,
                           StringBuilder str) {
        str.append(String.format("%2d", id));
        if (accepting) str.append("*|| ");
        else str.append(" || ");
        if ( !emptyEdges.isEmpty() ) str.append("ε");
        for (ENFAnode epsEdge : emptyEdges) {
            str.append("-");
            str.append(epsEdge.id);
            if (!alreadyQueuedNodes[epsEdge.id]) {
                queue.add(epsEdge);
                alreadyQueuedNodes[epsEdge.id] = true;
            }
        }
        if ( hasLetterEdge ) {
            if (edgeLetters.length == 1) str.append(edgeLetters[0]);
            else str.append("all");
            str.append("-");
            str.append(edge.id);
            if (!alreadyQueuedNodes[edge.id]) {
                queue.add(edge);
                alreadyQueuedNodes[edge.id] = true;
            }
        }
        str.append("\n");
    }

    // Create edge entries for this node, for the Graphviz format.
    void toGVstring(StringBuilder str) {
        String prefix = String.valueOf(id) + " -> ";
        if (hasLetterEdge) {
            str.append(prefix);
            str.append(edge.id);
            str.append(" [ label = \"");
            Character c = edgeLetters[0];
            str.append(edgeLetters.length>1 ? "Σ" : c == '\\' || c == '\"' ? "\\" + c.toString() : c.toString());
            str.append("\" ];");
            str.append(System.lineSeparator());
        } else {
            String suffix = " [ label = \"ε\" ];" + System.lineSeparator();
            for (ENFAnode node : emptyEdges) {
                str.append(prefix);
                str.append(node.id);
                str.append(suffix);
            }
        }
    }

    public void addEmptyEdge(ENFAnode emptyEdge) {
        emptyEdges.add(emptyEdge);
    }

    /* Get epsilon-closure of this node. It is appended to reachables. This is
     * a recursive construction so we also keep a list of queued nodes, that
     * we fill upp with the nodes that are epsilon-reachable from this node,
     * and a list of already queued nodes so we don't queue stuff up twice. */
    boolean getReachables(Map<Character, Set<ENFAnode>> reachables,
                       boolean[] alreadyQueuedNodes,
                       Queue<ENFAnode> queue) {
        if (hasLetterEdge) for (Character c : edgeLetters)
            try {
                reachables.get(c).add(edge);
            } catch (NullPointerException e) {
                System.err.println(String.format("Regex symbol not in alphabet: '%c'", c));
                System.err.println();
                throw e;
            }
        for (ENFAnode epsEdge : emptyEdges) {
            if (!alreadyQueuedNodes[epsEdge.id]) {
                queue.add(epsEdge);
                alreadyQueuedNodes[epsEdge.id] = true;
            }
        }
        if ( queue.isEmpty() ) return accepting;
        else return  queue.remove().getReachables(reachables, alreadyQueuedNodes, queue) || accepting;
    }

}

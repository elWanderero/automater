package eNFAgraph;

import java.util.*;

public class ENFAnode {
    public boolean accepting = false;
    public ENFAnode edge;
    public final Character[] edgeLetters;
    public boolean hasLetterEdge;
    public ArrayList<ENFAnode> emptyEdges = new ArrayList<>();

    // For nfa.NFA creation
    public int id;

    // Construct node without edges
    public ENFAnode(int id) {
        this.id = id;
        hasLetterEdge = false;
        edgeLetters = null;
    }
    // Construct node with one lettered edge
    public ENFAnode(int id, ENFAnode edge, Character[] edgeLetters) {
        this.id = id;
        hasLetterEdge = true;
        this.edge = edge;
        this.edgeLetters = edgeLetters;
    }
    // Construct node with one epsilon-edge
    public ENFAnode(int id, ENFAnode epsilonEdge) {
        this.id = id;
        hasLetterEdge = false;
        emptyEdges.add(epsilonEdge);
        edgeLetters = null;
    }

    protected void toStringBuilder(boolean[] alreadyQueuedNodes,
                           LinkedList<ENFAnode> queue,
                           StringBuilder str) {
        str.append(String.format("%2d", id));
        if (accepting) str.append("*||");
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
//            str.append(edgeLetters[0]);
            str.append("-");
            str.append(edge.id);
            if (!alreadyQueuedNodes[edge.id]) {
                queue.add(edge);
                alreadyQueuedNodes[edge.id] = true;
            }
        }
        str.append("\n");
    }

    public void addEmptyEdge(ENFAnode emptyEdge) {
        emptyEdges.add(emptyEdge);
    }

    protected void getReachables(Dictionary<Character, Set<ENFAnode>> reachables,
                                 boolean[] alreadyQueuedNodes,
                                 LinkedList<ENFAnode> queue) {
        if (hasLetterEdge) for (Character c : edgeLetters) reachables.get(c).add(edge);
        for (ENFAnode epsEdge : emptyEdges)
            if (!alreadyQueuedNodes[epsEdge.id]) {
                queue.add(epsEdge);
                alreadyQueuedNodes[epsEdge.id] = true;
            }
        if (!queue.isEmpty()) {
            ENFAnode next = queue.remove();
            next.getReachables(reachables, alreadyQueuedNodes, queue);
        }
    }

}

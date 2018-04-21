package eNFAgraph;

import java.util.*;
import nfa.NFA;

public class ENFA {
    public ENFAnode startNode;
    public ENFAnode acceptNode;
    private final Character[] alphabet;
    public int size;

    public ENFA(Character[] alphabet) {
        this.alphabet = alphabet;
        size = 0;
    }

    public ENFAnode newNode(ENFAnode edge, String type) {
        if (type.length() == 1) {  // one-letter edge
            return new ENFAnode(getId(), edge, new Character[] {type.charAt(0)});
        } else if (type.equals("alphabet")) {
            return new ENFAnode(getId(), edge, alphabet);
        } else if (type.equals("epsilon")) {
           return new ENFAnode(getId(), edge);
        } else throw new IllegalArgumentException("Allowed types: \"alphabet\", \"epsilon\" and any one-char string.");
    }
    public ENFAnode newNode() {
        return new ENFAnode(getId());
    }
    private int getId() { return size++; }

    public String toString() {
        StringBuilder str = new StringBuilder();
        LinkedList<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueuedNodes = new boolean[size];
        queue.add(startNode);
        alreadyQueuedNodes[startNode.id] = true;

        while (!queue.isEmpty()) queue.remove().toStringBuilder(alreadyQueuedNodes, queue, str);

        return str.toString();
    }

    /* The epsilon-ordering-classes (which are the NFA nodes) get represented by their root, i.e. the node that was
     * first discovered when traversing the e-NFA. However, if the root is actually part of a cycle, that representation
     * is not well-defined. Potentially then, we could end up with multiple NFA nodes that are actually the same class,
     * only because we represented them with different e-NFA nodes. In the case of an e-NFA generated from a regexp
     * however, this will never happen. We only evaluate the class of a node if it is the start node or is pointed at by
     * a lettered edge. And lettered edges can never point at e-cycles, which can be seen by inspecting the regex->eNFA
     * schema. Thus at most one discovered node (the start) will ever be part of a cycle.
     */
    public NFA toNFA() {
        Dictionary<Character, SortedSet<Integer>>[] nfa = new Hashtable[size];
        boolean[] acceptingStates = new boolean[size];

        LinkedList<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueuedNodes = new boolean[size];
        queue.add(startNode);
        alreadyQueuedNodes[startNode.id] = true;

        while (!queue.isEmpty()) {
            // next eNFA node for which to find epsilon-reachable subgraph.
            ENFAnode next = queue.remove();

            // Fill edgeDict with edges reached from next.
            Hashtable<Character, Set<ENFAnode>> edgeDict = new Hashtable<>(alphabet.length, 1);
            for (Character c : alphabet) edgeDict.put(c, new LinkedHashSet<>());
            boolean[] subAlreadyQueuedNodes = new boolean[size];  // Default values = false
            subAlreadyQueuedNodes[next.id] = true;
            next.getReachables(edgeDict, subAlreadyQueuedNodes, new LinkedList<>());

            Dictionary<Character, SortedSet<Integer>> nfaNode = new Hashtable<>(alphabet.length, 1);

            // Fill queue with new reached edges, and put their ids into the nfa.NFA
            for (Character c : alphabet) {
                Set<ENFAnode> reachedNodes = edgeDict.get(c);
                SortedSet<Integer> reachedNodeIds = new TreeSet<>();
                for (ENFAnode reachedNode : reachedNodes) {
                    if (!alreadyQueuedNodes[reachedNode.id]) {
                        queue.add(reachedNode);
                        alreadyQueuedNodes[reachedNode.id] = true;
                    }
                    reachedNodeIds.add(reachedNode.id);
                }
                nfaNode.put(c, reachedNodeIds);
            }

            nfa[next.id] = nfaNode;
            acceptingStates[next.id] = next.accepting;
        }

        return new NFA(acceptingStates, startNode.id, nfa, alphabet);
    }

}

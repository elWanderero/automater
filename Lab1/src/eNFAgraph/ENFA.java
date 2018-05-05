package eNFAgraph;

import java.util.*;

import nfa.NFA;
import nfa.EquivalenceClass;

public class ENFA {
    public ENFAnode startNode;
    public ENFAnode acceptNode;
    private final Character[] alphabet;
    public int size;

    public ENFA(Character[] alphabet) {
        this.alphabet = alphabet;
        size = 0;
    }

    public void initiate() { acceptNode.accepting = true; }

    public ENFAnode newNode(ENFAnode edge, String type) {
        if (type.length() == 1) {  // one-letter edge
            return new ENFAnode(newId(), edge, new Character[] {type.charAt(0)});
        } else if (type.equals("alphabet")) {
            return new ENFAnode(newId(), edge, alphabet);
        } else if (type.equals("epsilon")) {
           return new ENFAnode(newId(), edge);
        } else throw new IllegalArgumentException("Allowed types: \"alphabet\", \"epsilon\" and any one-char string.");
    }
    public ENFAnode newNode() {
        return new ENFAnode(newId());
    }
    private int newId() { return size++; }

    public String toString() {
        StringBuilder str = new StringBuilder();
        LinkedList<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueuedNodes = new boolean[size];
        queue.add(startNode);
        alreadyQueuedNodes[startNode.id] = true;

        while (!queue.isEmpty()) queue.remove().toStringBuilder(alreadyQueuedNodes, queue, str);

        return str.toString();
    }

    public static StringBuilder gvPrefix(int startNodeId, Collection<Integer> acceptNodeIds ) {
        String line = System.lineSeparator();
        StringBuilder str = new StringBuilder("digraph finite_state_machine {");
        str.append(line);
        str.append("rankdir=LR; size=\"19,11\"");
        str.append(line);
        str.append("node [shape = cds]; ");
        str.append(startNodeId);
        str.append(";");
        str.append(line);
        if (!acceptNodeIds.isEmpty()) str.append("node [shape = doublecircle]; ");
        for (Integer id : acceptNodeIds) {
            str.append(id);
            str.append(";");
        }
        if (!acceptNodeIds.isEmpty()) str.append(line);
        str.append("node [shape = circle];");
        str.append(line);
        return str;
    }

    public String toGVstring() {
        Queue<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueued = new boolean[size];
        queue.add(startNode);
        alreadyQueued[startNode.id] = true;

        List<Integer> acceptNodeIds = new ArrayList<>(1);
        acceptNodeIds.add(acceptNode.id);
        StringBuilder str = gvPrefix(startNode.id, acceptNodeIds);

        while ( !queue.isEmpty() ) {
            ENFAnode node = queue.remove();
            node.toGVstring(str);
            if (node.hasLetterEdge) {
                if (!alreadyQueued[node.edge.id]) {
                    queue.add(node.edge);
                    alreadyQueued[node.edge.id] = true;
                }
            } else {
                for (ENFAnode edge : node.emptyEdges) {
                    if (!alreadyQueued[edge.id]) {
                        queue.add(edge);
                        alreadyQueued[edge.id] = true;
                    }
                }
            }
        }
        str.append("}");
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
        @SuppressWarnings("unchecked")
        Map<Character, EquivalenceClass>[] nfa = new HashMap[size];
        boolean[] acceptingStates = new boolean[size];

        Queue<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueuedNodes = new boolean[size];
        queue.add(startNode);
        alreadyQueuedNodes[startNode.id] = true;

        while (!queue.isEmpty()) {
            // next eNFA node for which to find epsilon-reachable subgraph class.
            ENFAnode next = queue.remove();

            // Fill edgeDict with edges reached from next.
            Map<Character, Set<ENFAnode>> edgeDict = new HashMap<>(alphabet.length, 1);
            for (Character c : alphabet) edgeDict.put(c, new LinkedHashSet<>());
            boolean[] subAlreadyQueuedNodes = new boolean[size];  // Default values = false
            subAlreadyQueuedNodes[next.id] = true;
            boolean classAccepts = next.getReachables(edgeDict, subAlreadyQueuedNodes, new LinkedList<>());

            Map<Character, EquivalenceClass> nfaNode = new HashMap<>(alphabet.length, 1);

            // Fill queue with new reached edges, and put their ids into the nfa.NFA
            for (Character c : alphabet) {
                Set<ENFAnode> reachedNodes = edgeDict.get(c);
                EquivalenceClass reachedNodeIds = new EquivalenceClass();
                for (ENFAnode reachedNode : reachedNodes) {
                    if (!alreadyQueuedNodes[reachedNode.id]) {
                        queue.add(reachedNode);
                        alreadyQueuedNodes[reachedNode.id] = true;
                    }
                    reachedNodeIds.add(reachedNode.id);
                }
                nfaNode.put(c, reachedNodeIds);
            }
            acceptingStates[next.id] = classAccepts;
            nfa[next.id] = nfaNode;
        }

        return new NFA(alphabet, nfa, startNode.id, acceptingStates);
    }

}

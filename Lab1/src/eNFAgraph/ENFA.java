package eNFAgraph;

import java.util.*;

import nfa.NFA;
import nfa.EquivalenceClass;

/**
 * A container class for ENFAnodes, representing epsilon-non-determistic finitie automata.
 * This container class keeps track of the starting and accepting node, and the alphabet
 * and the size. ENFAnodes are supposed to be instantiated through this class, so that it
 * can implement the correct alphabet, keep track of size, and generally set things up in
 * the right way. Also contains some usefull method such as toString, a graphviz string
 * generator, and most importanly: A method to turn itself into an eqivalent NFA (without
 * epsilons) by reducing epsilon-closures to nodes.
 *
 * It was natural to implement it as a graph of objects, since resyntax is implemented in
 * the same way and so it was very easy to just add some code to those nodes and generate
 * ENFAnodes directly.
 */
public class ENFA {
    public ENFAnode startNode;
    public ENFAnode acceptNode;
    private final Character[] alphabet;
    public int size;

    public ENFA(Character[] alphabet) {
        this.alphabet = alphabet;
        size = 0;
    }

    /* Since we know there will be exactly one accepting edge, we don't bother setting and unsetting the accepting
     * property of nodes during the recursive construction. Instead we require it to be manually done after
     * construction is finished. */
    public void initiate() { acceptNode.accepting = true; }

    /* The special kinds of e-NFA's we need are such that every node can have a bunch of epsilon-edges, but only one
     * alphabet-lettered edge. Further, this edge will be labeled by either one single letter or the entire alphabet.
     * Thus this constructor is initiated with one single outgoing edge, which can be of type "epsilon", "alphabet"
     * or a single letter which will then be the label letter. */
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

    public String toString() {
        StringBuilder str = new StringBuilder();
        LinkedList<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueuedNodes = new boolean[size];
        queue.add(startNode);
        alreadyQueuedNodes[startNode.id] = true;

        while (!queue.isEmpty()) queue.remove().toStringBuilder(alreadyQueuedNodes, queue, str);

        return str.toString();
    }

    // Creates the header of a gv-file. Implemented as a separate function so that I can import and use it in other
    // places, but only need to change the details here.
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

    // Return a Graphviz .gv-formatted string, ready for dot.
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

    /* The epsilon-ordering-classes (which become the NFA nodes) get represented by their root, i.e. the node that was
     * first discovered when traversing the e-NFA. However, if the root is actually part of a cycle, that representation
     * is not well-defined. Potentially then, we could end up with multiple NFA nodes that are actually the same class,
     * only because we represented them with different e-NFA nodes. In the case of an e-NFA generated from a regexp
     * however, this will never happen. We only evaluate the class of a node if it is the start node or is pointed at by
     * a lettered edge. And lettered edges can never point at e-cycles, which can be seen by inspecting the regex->eNFA
     * schema. Thus at most one discovered node (the start) will ever be part of a cycle.
     *
     * Starting from the startNode, find the epsilon-closure, and put all nodes pointed at from the epsilon-closure
     * in a queue. Iterate trough queue until it is empty. To avoid putting a closure in the queue twice we keep a
     * list of already queued nodes.
     */
    public NFA toNFA() {
        @SuppressWarnings("unchecked")
        Map<Character, EquivalenceClass>[] nfa = new HashMap[size];  // edges of the new NFA
        boolean[] acceptingStates = new boolean[size];

        Queue<ENFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueuedNodes = new boolean[size];
        queue.add(startNode);
        alreadyQueuedNodes[startNode.id] = true;

        while (!queue.isEmpty()) {
            // next eNFA node for which to find epsilon-closure.
            ENFAnode next = queue.remove();

            // Fill edgeDict with edges reached from next.
            Map<Character, Set<ENFAnode>> edgeDict = new HashMap<>(alphabet.length, 1);
            for (Character c : alphabet) edgeDict.put(c, new LinkedHashSet<>());
            boolean[] subAlreadyQueuedNodes = new boolean[size];  // Default values = false
            subAlreadyQueuedNodes[next.id] = true;
            boolean classAccepts = next.getReachables(edgeDict, subAlreadyQueuedNodes, new LinkedList<>());

            Map<Character, EquivalenceClass> nfaNode = new HashMap<>(alphabet.length, 1);

            // Fill queue with new reached edges, and put their ids into the nfa table
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
                nfaNode.put(c, reachedNodeIds);  // This is where we put the edges of the new nfa node
            }
            acceptingStates[next.id] = classAccepts;
            nfa[next.id] = nfaNode;
        }

        return new NFA(alphabet, nfa, startNode.id, acceptingStates);
    }

    private int newId() { return size++; }

}

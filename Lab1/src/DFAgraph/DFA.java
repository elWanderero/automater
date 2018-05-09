package DFAgraph;

import eNFAgraph.ENFA;

import java.util.*;

/**
 * A container class for an deterministic automaton. The DFA is implemented as a graph
 * of DFAnodes, but this class is for proper initialisation (new nodes should be
 * constructed using makeNode) and keeping track of things like the start, possible
 * death states, size, alphabet, and the very important minimise() method.
 *
 * It also keeps track of the mode of evaluation (strong for entire string must fit,
 * weak for accept immediately once an accepting state is reached.) Note that weak
 * still only searches all possible prefixes. To evaluate all substrings, the regex
 * ".*(<string>)" should pe prepended to the original string, as can be done with the
 * compile() method in Main.
 *
 * Java apparently does not optimise tail-recursion. So probably a table would have been
 * faster. But I wanted a proper graph of objects because it felt cooler, and closer to
 * the theory.
 */
public class DFA {
    private DFAnode start;
    private DFAnode deathNode;
    private List<DFAnode> nodesList;
    private final Character[] alphabet;
    private int size;
    public boolean strongEvaluation;  // strong <-> no substring search

    public DFA(Character[] alphabet) { this(alphabet,false); }

    public DFA(Character[] alphabet, boolean withDeath) {
        this.alphabet = alphabet;
        this.nodesList = new LinkedList<>();
        size = 0;
        if ( withDeath ) initiateDeathNode(alphabet, nodesList);
        strongEvaluation = false;
    }

    private void initiateDeathNode(Character[] alphabet, List<DFAnode> nodesList) {
        this.deathNode = new DeathNode(alphabet, newId());
        nodesList.add(deathNode);
        Map<Character, DFAnode> selfEdges = new HashMap<>(alphabet.length, 1);
        for ( Character c: alphabet) selfEdges.put(c, deathNode);
        deathNode.edges = selfEdges;
    }

    public DFAnode makeNode() {
        DFAnode newNode = new DFAnode(alphabet, newId());
        nodesList.add(newNode);
        return newNode;
    }

    public DFAnode getDeathNode() {
        return deathNode;
    }

    public void setDeathNode(DFAnode deathNode) { this.deathNode = deathNode; }

    public void setStart(DFAnode start) {
        this.start = start;
    }

    public boolean eval(String str) { return strongEvaluation ? strongEval(str) : weakEval(str); }

    // Evaluate entire string. So only return true if at accepting state at last char.
    private boolean strongEval(String str) { return start.strongEval(str, 0); }

    // Return true immediately upon reaching an accepting state.
    private boolean weakEval(String str) { return start.weakEval(str, 0); }

    private boolean newEdgeDifference(DFAnode a, DFAnode b, boolean[][] diffTable) {
        if ( diffTable[a.id][b.id] ) return false;
        for ( Character c : alphabet ) {
            if (diffTable[a.edges.get(c).id][b.edges.get(c).id]) return true;
        }
        return false;
    }

    /* Using the table filling method, where we have a triangular nodes*nodes matrix
     * diffTable. We mark every entry in the table for which the two corresponding
     * nodes differ on whether they accept or nor (with boolean true, for "yes they
     * are different.") This is step 0. We then iteratively go through the table over
     * and over, at each iteration marking two nodes as different if for any letter
     * their edges point at two nodes that are already marked as different. We stop
     * when for some iteration nothing changes (This must happen at some point since
     * either we strictly increase the number of marked table entries, or nothing
     * changes during an iteration.)
     *
     * Having finished the table, the relation of being not-different is reflexive
     * and transitive, i.e. an equivalence relation (Myhill-Nerode equivalence even.)
     * Reflexiveness is obvious, and transitiveness can be shown inductively, but
     * pretty much it is transitive because we considered elements different at some
     * point if any of their edges were considered different, and this property
     * transfers. We then form a new automaton whose nodes are these equivalence
     * classes. Since equivalence classes were formed on their edges pointing to the
     * same class, this new automata also preserves edges. By the first step of the
     * table construction, it also preserves acceptance/non-acceptance. So this quotient
     * automaton (quoted  out on the mentioned equivalence relation) is equivalent to
     * the old automaton, but smaller. By the Myhill-Nerode theorem it is in fact THE
     * smallest equivalent automaton. BAM DONE!
     *
     * Implementation technicalities: If deathNode is the first in its equivalence
     * class, ordered by index in nodesList, then the returned DFA will have a proper
     * DeathNode for deathNode, which can make failed evaluations faster.
     */
    public DFA minimise() {
        // So we surely have O(1) random access.
        DFAnode[] fastList = nodesList.toArray(new DFAnode[size]);

        /* Compute equivalence class table
         */
        boolean[][] diffTable = new boolean[size][size];
        // First iteration is special since it doesn't look at edges.
        for ( int i=0 ; i<size ; ++i )
            for ( int j=i+1  ; j<size ; j++ ) {
                diffTable[i][j] = fastList[i].accepting ^ fastList[j].accepting;
            }
        // Iteratively fill the table until nothing changes during a run.
        boolean anythingChanged = true;
        while (anythingChanged) {
            anythingChanged = false;
            for ( int i=0 ; i<size ; ++i ) {
                for (int j = i + 1; j < size; j++)
                    if (newEdgeDifference(fastList[i], fastList[j], diffTable)) {
                        diffTable[i][j] = true;
                        diffTable[j][i] = true;
                        anythingChanged = true;
                    }
            }
        }

        /* Construct the equivalence classes and new DFA nodes.
         */
        DFA minimalDFA = new DFA(alphabet, true);
        boolean[] alreadyClassified = new boolean[size];  // Populated with false at creation.
        // Equivalence classes are represented by it's earliest member, as ordered in fastList.
        // This element is called root, and all elements will eventually point to one.
        List<DFAnode> rootList = new LinkedList<>();
        // Map from the roots to the actual new nodes. Bijective.
        Map<DFAnode, DFAnode> newNodePointers = new HashMap<>(size, 1);
        for ( int i=0 ; i<size ; ++i ) if ( !alreadyClassified[i] ) {
            alreadyClassified[i] = true;
            DFAnode currNode = nodesList.get(i);
            // If we should encounter a deathNode, we make sure the corresponding class is also a deathNode.
            DFAnode newNode = currNode == deathNode ? minimalDFA.deathNode : minimalDFA.makeNode();
            newNode.accepting = currNode.accepting;
            newNodePointers.put(currNode, newNode);
            rootList.add(currNode);
            for (int j = i + 1; j < size; ++j) if (!diffTable[i][j]) {
                alreadyClassified[j] = true;
                newNodePointers.put(nodesList.get(j), newNode);
                if (nodesList.get(j).accepting) newNode.accepting = true;
            }
        }

        /* Wire the edges of the new graph, by copying the edges of the root, and re-pointing
           every edge like so: edge = oldNode -> root of oldNode -> new node of root = new Edge
         */
        for ( DFAnode root : rootList ) {
            Map<Character, DFAnode> newEdges = new HashMap<>(root.edges);
            newEdges.replaceAll((c, oldNode) -> newNodePointers.get(oldNode));
            newNodePointers.get(root).edges = newEdges;
        }

        minimalDFA.setStart(newNodePointers.get(start));
        // Even if we didn't manage to construct an actual DeathNode, we can still nominally set the deatNode.
        if (deathNode != null) minimalDFA.setDeathNode(newNodePointers.get(deathNode));
        return minimalDFA;
    }

    @Override
    public String toString() { return toString(false); }

    public String toString(boolean includeDeath) {
        if ( includeDeath && deathNode == null ) return  toString(false);
        int idOfDeath = deathNode == null ? 0 : deathNode.id;
        if ( start == null ) return "[Empty DFA]";
        StringBuilder str = new StringBuilder();
        Queue<DFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueued = new boolean[size];
        queue.add(start);
        alreadyQueued[start.id] = true;
        while ( !queue.isEmpty() ) {
            DFAnode node = queue.remove();
            if (includeDeath) node.addToStringBuilder(str);
            else node.addToStringBuilderExcludeNode(str, idOfDeath);
            for ( DFAnode edge : node.edges.values() ) {
                if ( !alreadyQueued[edge.id] ) {
                    queue.add(edge);
                    alreadyQueued[edge.id] = true;
                }
            }
        }
        str.replace(0,1, ">");  // Start symbol
        return str.toString();
    }

    // Return a Graphviz .gv-formatted string, ready for dot.
    public String toGVstring() {
        Queue<DFAnode> queue = new LinkedList<>();
        boolean[] alreadyQueued = new boolean[size];
        queue.add(start);
        alreadyQueued[start.id] = true;

        StringBuilder str = ENFA.gvPrefix(start.id, new ArrayList<>(0));

        while ( !queue.isEmpty() ) {
            DFAnode node = queue.remove();
            node.addToGVstringBuilder(str);
            for ( DFAnode edge : node.edges.values() ) {
                if ( !alreadyQueued[edge.id] ) {
                    queue.add(edge);
                    alreadyQueued[edge.id] = true;
                }
            }
        }
        str.append("}");
        return str.toString();
    }

    private int newId() { return size++; }

}

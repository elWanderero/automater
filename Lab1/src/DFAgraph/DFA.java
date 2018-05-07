package DFAgraph;

import eNFAgraph.ENFA;

import java.util.*;

public class DFA {
    private DFAnode start;
    private DFAnode deathNode;
    private List<DFAnode> nodesList;
    private final Character[] alphabet;
    private int size;
    public boolean strongEvaluation;

    public DFA(Character[] alphabet) { this(alphabet,false); }

    public DFA(Character[] alphabet, boolean withDeath) {
        this.alphabet = alphabet;
        this.nodesList = new LinkedList<>();
        size = 0;
        if ( withDeath ) initiateDeathNode(alphabet, nodesList);
        strongEvaluation = true;
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

    private boolean strongEval(String str) { return start.strongEval(str, 0); }

    private boolean weakEval(String str) { return start.weakEval(str, 0); }

    private boolean newEdgeDifference(DFAnode a, DFAnode b, boolean[][] diffTable) {
        if ( diffTable[a.id][b.id] ) return false;
        for ( Character c : alphabet ) {
            if (diffTable[a.edges.get(c).id][b.edges.get(c).id]) return true;
        }
        return false;
    }

    // Using the table filling method. If deathNode is the first in its equivalence class, ordered by index
    // in nodesList, then the returned DFA will have a probed DeathNode for deathNode.
    public DFA minimise() {
        DFAnode[] fastList = nodesList.toArray(new DFAnode[size]);
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
        // Create the equivalence classes and new DFA nodes.
        DFA minimalDFA = new DFA(alphabet, true);
        boolean[] alreadyClassified = new boolean[size];  // Populated with false at creation.
        Map<DFAnode, DFAnode> newNodePointers = new HashMap<>(size, 1);
        List<DFAnode> rootList = new LinkedList<>();
        for ( int i=0 ; i<size ; ++i ) if ( !alreadyClassified[i] ) {
            alreadyClassified[i] = true;
            DFAnode currNode = nodesList.get(i);
            //
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
        // Wire the edges
        for ( DFAnode root : rootList ) {
            Map<Character, DFAnode> newEdges = new HashMap<>(root.edges);
            newEdges.replaceAll((c, oldNode) -> newNodePointers.get(oldNode));
            newNodePointers.get(root).edges = newEdges;
        }
        minimalDFA.setStart(newNodePointers.get(start));
        minimalDFA.setDeathNode(newNodePointers.get(deathNode));
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

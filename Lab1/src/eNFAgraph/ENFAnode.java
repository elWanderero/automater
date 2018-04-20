package eNFAgraph;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedHashSet;

public class ENFAnode {
    public boolean accepting = false;
    public ENFAnode edge;
    public final Character[] edgeLetters;
    public boolean hasLetterEdge;
    public ArrayList<ENFAnode> emptyEdges = new ArrayList<>();

    // For NFA creation
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

    public void addEmptyEdge(ENFAnode emptyEdge) {
        emptyEdges.add(emptyEdge);
    }

    public void getReachables(Dictionary<Character, ArrayList<ENFAnode>> letteredEdgeDict) {

    }

}

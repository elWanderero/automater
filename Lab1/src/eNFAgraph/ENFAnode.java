package eNFAgraph;

import java.util.ArrayList;

public class ENFAnode {
    public boolean accepting = false;
    public ENFAnode edge;
    public ArrayList<Character> edgeLetters;
    public boolean hasLetterEdge;
    public ArrayList<ENFAnode> emptyEdges;

    // Construct node without edges
    public ENFAnode() {
        hasLetterEdge = false;
        emptyEdges = new ArrayList<>();
    }

    // Construct node with one lettered edge
    public ENFAnode(ENFAnode letteredEdge, Character letter) {
        hasLetterEdge = true;
        edge = letteredEdge;
        edgeLetters = new ArrayList<>(letter);
    }

    // Construct node with one edge with several letters
    public ENFAnode(ENFAnode multiLetteredEdge, ArrayList<Character> edgeLetters) {
        hasLetterEdge = true;
        edge = multiLetteredEdge;
        this.edgeLetters = edgeLetters;
    }

    // Construct node with one empty edge
    public ENFAnode(ENFAnode emptyEdge) {
        hasLetterEdge = false;
        emptyEdges.add(emptyEdge);
    }

    // Construct node with two empty edges
    public ENFAnode(ENFAnode emptyEdge1, ENFAnode emptyEdge2) {
        hasLetterEdge = false;
        emptyEdges.add(emptyEdge1);
        emptyEdges.add(emptyEdge2);
    }

    public void addEmptyEdge(ENFAnode emptyEdge) {
        emptyEdges.add(emptyEdge);
    }
}

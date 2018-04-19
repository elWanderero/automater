package eNFAgraph;

import java.util.ArrayList;

public class ENFA {
    public ENFAnode startNode;
    public ENFAnode acceptNode;
    private final ArrayList<Character> alphabet;

    public ENFA(ArrayList<Character> alphabet) { this.alphabet = alphabet; }

    // Only get alphabet as a copy.
    public ArrayList<Character> alphabet() { return new ArrayList<>(alphabet); }
}

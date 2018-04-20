package eNFAgraph;

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
            return new ENFAnode(size++, edge, alphabet);
        } else if (type.equals("alphabet")) {
            return new ENFAnode(size++, edge, alphabet);
        } else if (type.equals("epsilon")) {
           return new ENFAnode(size++, edge);
        } else throw new IllegalArgumentException("Allowed types: \"alphabet\", \"epsilon\" and any one-char string.");
    }
    public ENFAnode newNode() {
        return new ENFAnode(size++);
    }

    // Only get alphabet as a copy.
//    public ArrayList<Character> alphabet() { return new ArrayList<>(alphabet); }
}

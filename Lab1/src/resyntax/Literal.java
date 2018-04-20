package resyntax;

import eNFAgraph.ENFA;
import eNFAgraph.ENFAnode;

public class Literal extends RegExp {
    public final Character c;
    public Literal(Character c) {
        this.c = c;
    }

    @Override
    public void toStringBuilder(StringBuilder strB) {
        strB.append(c);
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        ENFAnode end = eAutomat.newNode();
        eAutomat.startNode = eAutomat.newNode(end, String.valueOf(c));
        eAutomat.acceptNode = end;
    }
}

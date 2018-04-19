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
        ENFAnode end = new ENFAnode();
        eAutomat.startNode = new ENFAnode(end, c);
        eAutomat.acceptNode = end;
    }
}

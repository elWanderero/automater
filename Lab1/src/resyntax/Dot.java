package resyntax;

import eNFAgraph.ENFA;
import eNFAgraph.ENFAnode;

public class Dot extends RegExp {

    @Override
    public void toStringBuilder(StringBuilder strB) {
        strB.append('.');
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        ENFAnode end = eAutomat.newNode();
        eAutomat.startNode = eAutomat.newNode(end, "alphabet");
        eAutomat.acceptNode = end;
    }
}

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
        ENFAnode end = new ENFAnode();
        eAutomat.startNode = new ENFAnode(end, eAutomat.alphabet());
        eAutomat.acceptNode = end;
    }
}

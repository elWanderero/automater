package resyntax;

import eNFAgraph.ENFA;
import eNFAgraph.ENFAnode;

public class Closure extends RegExp {
    public final RegExp r;
    public Closure(RegExp r) {
        this.r = r;
    }

    @Override
    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r.toStringBuilder(strB);
        strB.append(")*");
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        r.toENFA(eAutomat);
        ENFAnode end = new ENFAnode();
        ENFAnode start = new ENFAnode(eAutomat.startNode, end);
        eAutomat.acceptNode.addEmptyEdge(eAutomat.startNode);
        eAutomat.acceptNode.addEmptyEdge(end);
        eAutomat.startNode = start;
        eAutomat.acceptNode = end;
    }
}

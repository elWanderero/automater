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
        ENFAnode end = eAutomat.newNode();
        ENFAnode start = eAutomat.newNode(end, "epsilon");
        start.addEmptyEdge(eAutomat.startNode);
        eAutomat.acceptNode.addEmptyEdge(eAutomat.startNode);
        eAutomat.acceptNode.addEmptyEdge(end);
        eAutomat.startNode = start;
        eAutomat.acceptNode = end;
    }
}

package resyntax;

import eNFAgraph.ENFA;
import eNFAgraph.ENFAnode;

public class ZeroOrOne extends RegExp {
    public final RegExp r;
    public ZeroOrOne(RegExp r) {
        this.r = r;
    }

    @Override
    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r.toStringBuilder(strB);
        strB.append(")?");
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        r.toENFA(eAutomat);
        ENFAnode end = new ENFAnode();
        ENFAnode start = new ENFAnode(eAutomat.startNode, end);
        eAutomat.acceptNode.addEmptyEdge(end);
        eAutomat.startNode = start;
        eAutomat.acceptNode = end;
    }
}

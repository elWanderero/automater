package resyntax;

import eNFAgraph.ENFA;

public class OneOrMore extends RegExp {
    public final RegExp r;
    public OneOrMore(RegExp r) {
        this.r = r;
    }

    @Override
    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r.toStringBuilder(strB);
        strB.append(")+");
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        r.toENFA(eAutomat);
        eAutomat.acceptNode.addEmptyEdge(eAutomat.startNode);
    }
}

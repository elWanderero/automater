package resyntax;

import eNFAgraph.ENFA;
import eNFAgraph.ENFAnode;

public class Union extends RegExp {
    public final RegExp r1, r2;
    public Union(RegExp r1, RegExp r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    @Override
    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r1.toStringBuilder(strB);
        strB.append(")|(");
        r2.toStringBuilder(strB);
        strB.append(')');
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        r1.toENFA(eAutomat);
        ENFAnode start = eAutomat.newNode(eAutomat.startNode, "epsilon");
        ENFAnode end = eAutomat.newNode();
        eAutomat.acceptNode.addEmptyEdge(end);
        r2.toENFA(eAutomat);
        start.addEmptyEdge(eAutomat.startNode);
        eAutomat.acceptNode.addEmptyEdge(end);
        eAutomat.startNode = start;
        eAutomat.acceptNode = end;
    }
}

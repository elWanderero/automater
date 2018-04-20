package resyntax;

import eNFAgraph.ENFA;
import eNFAgraph.ENFAnode;

public class Concatenation extends RegExp {
    public final RegExp r1, r2;
    public Concatenation(RegExp r1, RegExp r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    @Override
    public void toStringBuilder(StringBuilder strB) {
        r1.toStringBuilder(strB);
        r2.toStringBuilder(strB);
    }

    @Override
    public void toENFA(ENFA eAutomat) {
        r1.toENFA(eAutomat);
        ENFAnode start = eAutomat.newNode(eAutomat.startNode, "epsilon");
        ENFAnode end = eAutomat.acceptNode;
        r2.toENFA(eAutomat);
        end.addEmptyEdge(eAutomat.startNode);
        end = eAutomat.newNode();
        eAutomat.acceptNode.addEmptyEdge(end);
        eAutomat.startNode = start;
        eAutomat.acceptNode= end;
    }


}

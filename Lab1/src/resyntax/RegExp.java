package resyntax;

import eNFAgraph.ENFA;

public abstract class RegExp {
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
    public StringBuilder toStringBuilder() {
        StringBuilder strB = new StringBuilder();
        toStringBuilder(strB);
        return strB;
    }
    public abstract void toStringBuilder(StringBuilder strB);

    public abstract void toENFA(ENFA eAutomat);
}

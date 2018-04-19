package resyntax;

public class OneOrMore extends RegExp {
    public final RegExp r;
    public OneOrMore(RegExp r) {
        this.r = r;
    }

    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r.toStringBuilder(strB);
        strB.append(")+");
    }
}
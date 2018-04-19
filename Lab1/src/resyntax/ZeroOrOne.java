package resyntax;

public class ZeroOrOne extends RegExp {
    public final RegExp r;
    public ZeroOrOne(RegExp r) {
        this.r = r;
    }

    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r.toStringBuilder(strB);
        strB.append(")?");
    }
}

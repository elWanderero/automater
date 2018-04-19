package resyntax;

public class Closure extends RegExp {
    public final RegExp r;
    public Closure(RegExp r) {
        this.r = r;
    }

    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r.toStringBuilder(strB);
        strB.append(")*");
    }
}

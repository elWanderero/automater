package resyntax;

public class Union extends RegExp {
    public final RegExp r1, r2;
    public Union(RegExp r1, RegExp r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    public void toStringBuilder(StringBuilder strB) {
        strB.append('(');
        r1.toStringBuilder(strB);
        strB.append(")|(");
        r2.toStringBuilder(strB);
        strB.append(')');
    }
}

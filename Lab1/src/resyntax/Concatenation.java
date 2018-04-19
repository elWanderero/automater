package resyntax;

public class Concatenation extends RegExp {
    public final RegExp r1, r2;
    public Concatenation(RegExp r1, RegExp r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    public void toStringBuilder(StringBuilder strB) {
        r1.toStringBuilder(strB);
        r2.toStringBuilder(strB);
    }
}

package resyntax;

public class Litteral extends RegExp {
    public final Character c;
    public Litteral(Character c) {
        this.c = c;
    }

    public void toStringBuilder(StringBuilder strB) {
        strB.append(c);
    }
}

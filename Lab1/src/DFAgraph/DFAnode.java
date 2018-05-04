package DFAgraph;

import java.util.Map;
import java.util.HashMap;

public class DFAnode {
    public boolean accepting;
    public Map<Character, DFAnode> edges;
    public final int id;

    boolean eval (String str, int index) {
        if (index >= str.length()) return accepting;
        else return edges.get(str.charAt(index)).eval(str, ++index);
    }

    DFAnode(Character[] alphabet, int id) {
        this.edges = new HashMap<>(alphabet.length);
        this.id = id;
    }

    void addToStringBuilderExcludeNode(StringBuilder str, int idOfNodeToExclude) {
        if ( id == idOfNodeToExclude ) return;
        str.append(String.format("%2d", id));
        if (accepting) str.append("*||");
        else str.append(" ||");
        edges.forEach((c, node) -> str.append( node.id == idOfNodeToExclude ? "" : String.format(" %c →%2d |", c, node.id)));
        str.append(System.lineSeparator());
    }

    void addToStringBuilder(StringBuilder str) {
        str.append(String.format("%2d", id));
        if (accepting) str.append("*||");
        else str.append(" ||");
        edges.forEach((c, node) -> str.append(String.format(" %c →%2d |", c, node.id)));
        str.append(System.lineSeparator());
    }

}

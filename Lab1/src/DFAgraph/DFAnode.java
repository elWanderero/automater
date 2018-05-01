package DFAgraph;

import java.util.Map;
import java.util.HashMap;

public class DFAnode {
    public boolean accepting;
    public Map<Character, DFAnode> edges;
    public final int id;

    public  DFAnode(Character[] alphabet, int id) {
        this.edges = new HashMap<>(alphabet.length);
        this.id = id;
    }

    void addToStringBuilder(StringBuilder str) {
        str.append(String.format("%2d", id));
        if (accepting) str.append("*||");
        else str.append(" ||");
        edges.forEach((c, node) -> str.append( node.id == 0 ? "" : String.format(" %c →%2d |", c, node.id)));
        str.append(System.lineSeparator());
    }

}

package DFAgraph;

import java.util.Map;
import java.util.HashMap;

/**
 * Deterministic automaton node class. Note that it has no public methods, since
 * it is supposed to be handled by an NFA. It's properties are public however
 * so that others can inspect it, and construct other things from them.
 */
public class DFAnode {
    public boolean accepting;
    public Map<Character, DFAnode> edges;
    public final int id;

    // Returns true if node is accepting and we are at the very last
    // node. index keeps track of the current char in str to evaluate.
    boolean strongEval(String str, int index) {
        if (index >= str.length()) return accepting;
        else return edges.get(str.charAt(index)).strongEval(str, ++index);
    }

    // Returns true immediately if node is accepting. index keeps track of
    // the current char in str to evaluate.
    boolean weakEval (String str, int index) {
        if (accepting || index >= str.length()) return accepting;
        else return edges.get(str.charAt(index)).weakEval(str, ++index);
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

    void addToGVstringBuilder(StringBuilder str) {
        if (accepting) str.append(String.format("%d [shape = doublecircle];%n", id));
        edges.forEach((c, node) -> {
            String escapedChar = c == '\\' || c == '\"' ? "\\" + c.toString() : c.toString();
            str.append(String.format("%d -> %d [ label = \"%s\" ];%n", id, node.id, escapedChar));
        });
    }

}

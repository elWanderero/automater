import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.String.format;

public class DFA {

    String start = "";
    Set<String> acceptingNodes = new HashSet<>();
    Set<String> failNodes = new HashSet<>();
    private Map<String, Boolean> allNodes = new HashMap<>();  // boolean is for acceptingness
    public Set<DFAedge> allEdges = new HashSet<>();

    private static final String line = System.lineSeparator();

    public void addTransitions(Stream<String> edges) {
        edges.forEach((String str) -> {
            try { parse(str); }
            catch (ParseException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    ////////////////////////////////////////////////////
    //                   toGraphviz                   //
    ////////////////////////////////////////////////////

    public String toGVstring() {
        StringBuilder str = new StringBuilder("digraph spec_DFA {");
        str.append(format("%nrankdir=LR; size=\"19,11\"%n"));
        str.append(format("node [shape=doublecircle];%n"));
        for ( String node: acceptingNodes )
            str.append(format("%s; ", node));
        str.append(line);

        if ( !start.equals("") ) {
            str.append("node [shape=point]; invisibleStart;");
            str.append(format("%nnode [shape=circle];%n"));  // default node shape.
            str.append(format("invisibleStart->%s;%n", start));
        } else str.append(format("node [shape=circle];%n"));  // default node shape.
        for ( DFAedge e: allEdges )
            str.append(format("%s->%s [label=\"%s\"];%n", e.q0, e.q1, e.v.equals("eps")?"ε":e.v));
        return str.append("}").toString();
    }

    ////////////////////////////////////////////////////
    //                    toString                    //
    ////////////////////////////////////////////////////
    @Override
    public String toString() {
        boolean startAlreadyPrinted = false;
        StringBuilder str = new StringBuilder();
        for ( DFAedge edge : allEdges ) {
            startAlreadyPrinted = addStateToStrBuilder(str, edge.q0, startAlreadyPrinted);
            addEdgeToStrBuilder(str, edge.v);
            startAlreadyPrinted = addStateToStrBuilder(str, edge.q1, startAlreadyPrinted);
            str.append(System.lineSeparator());
        }
        return str.toString();
    }
    private void addEdgeToStrBuilder(StringBuilder str, String edgeLabel) {
        str.append(format("-%s->", edgeLabel));
    }
    private boolean addStateToStrBuilder(StringBuilder str, String state, boolean startAlreadyPrinted) {
        boolean startWasPrinted = false;
        if ( !startAlreadyPrinted && state.equals(start) ) {
            str.append("=>");
            startWasPrinted = true;
        }
        if ( allNodes.get(state) ) str.append(format("(%s)", state));
        else str.append(format("[%s]", state));
        return startWasPrinted;
    }

    ////////////////////////////////////////////////////
    //                    parsing                     //
    ////////////////////////////////////////////////////
    // Parse using recursive descent
    private void parse(String edgeStr) throws ParseException {
        // Empty edge ok, just doesn't do anything
        if ( edgeStr.length()==0 ) return;

        // Turns edgeStr into Queue of Characters.
        Queue<Character> chars = new LinkedList<>();
        for (int i=0 ; i<edgeStr.length() ; ++i) chars.add(edgeStr.charAt(i));

        String stateFrom;
        String edge;
        String stateTo;
        try {
            stateFrom = parseState(chars, false);
            edge = parseEdge(chars);
            stateTo = parseState(chars, false);
        } catch (ParseException e) {
            e.printStackTrace();
            String err_msg = format(
                    "Error in '%s'. %s",
                    edgeStr,
                    e.getMessage());
            throw new ParseException(err_msg, 0);
        }

        allEdges.add(new DFAedge(stateFrom, edge, stateTo));

    }

    private static String parseEdge(Queue<Character> chars) throws ParseException {
        checkForAndRemove(chars, '-');
        String label = parseLabel(chars);
        checkForAndRemove(chars, '-');
        checkForAndRemove(chars, '>');
        return label;
    }

    private String parseState(Queue<Character> chars, boolean start) throws ParseException {
        String stateLabel;
        // We expect =>(stateName), =>[stateName], (stateName) or [stateName]
        char next = removeAndCheckNonempty(chars);
        switch (next) {
            case '=':
                if (checkForAndRemove(chars, '>')) stateLabel = parseState(chars, true);
                else throw expectedGottenFormatException('>', next);
                break;
            case '(':
                stateLabel = parseLabel(chars);
                checkForAndRemove(chars,')');
                makeState(stateLabel, start, true);
                break;
            case '[':
                stateLabel = parseLabel(chars);
                checkForAndRemove(chars,']');
                makeState(stateLabel, start, false);
                break;
            default:
                String err_str = format(
                        "Expected state initialiser '=>', '(' or '['. Found '%c'",
                        next);
                throw new ParseException(err_str, 0);
        }
        return stateLabel;
    }

    private void makeState(String label, boolean starting, boolean accepting) throws ParseException {
        if ( allNodes.containsKey(label) ) {
            if (allNodes.get(label) != accepting) {
                String err_msg = format(
                        "Acceptingness of state '%s' inconsistent with previously used brackets",
                        label);
                throw new ParseException(err_msg, 0);
            }
        } else {
            allNodes.put(label, accepting);
            if ( accepting ) acceptingNodes.add(label);
            else failNodes.add(label);
        }
        if ( starting ) start = label;
    }

    private static ParseException expectedGottenFormatException(char expected, char gotten) {
        return new ParseException(format("Expected '%c', found '%c'.", expected, gotten), 0);
    }

    private static boolean compliesWithForbiddenLabelChars(char c) {
        return c != '(' && c!=')' && c!='[' && c!=']' && c!='=' && c!='>' && c!='-';
    }
    // Reads from Queue until it encounters a forbidden character (which include
    // state delimiters and null.) Returns all previous as a string, which presumably
    // is the intended name of some state being parsed.
    private static String parseLabel(Queue<Character> chars) throws ParseException {
        StringBuilder label = new StringBuilder();
        char next = chars.element();
        while ( compliesWithForbiddenLabelChars(next) ) {
            next = removeAndCheckNonempty(chars);
            label.append(next);
            next = chars.element();
        }
        if ( label.length()==0 ) throw new ParseException("Empty label name.", 0);
        return label.toString();
    }

    // Removes and returns next Character in Queue. If there is no next, throw exception
    private static char removeAndCheckNonempty(Queue<Character> chars) throws ParseException {
        if (chars.isEmpty()) throw new ParseException("Unexpected end of string", 0);
        return chars.remove();
    }

    // Checks that the next char in Queue is the expected one, and removes it.
    // Otherwise throws exception.
    private static boolean checkForAndRemove(Queue<Character> chars, char checkForThis) throws ParseException {
        char next = removeAndCheckNonempty(chars);
        if ( next==checkForThis ) return true;
        else throw expectedGottenFormatException(checkForThis, next);
    }

}

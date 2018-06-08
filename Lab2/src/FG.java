import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.String.format;

public class FG {
    private static final String line = System.lineSeparator();

    public MMMap<String, String, String> edges = new MMMap<>();

    public String mainEntryNode;
    public Map<String, String> methodEntryPoints = new HashMap<>();
    public Set<String> returnNodes = new HashSet<>();

    private Set<String> nodes = new HashSet<>();  // For checking for repeated declarations.
    private Set<String> methods = new HashSet<>();
    private Map<String, String> nodeMethods = new HashMap<>();

    public void addTransitions(Stream<String> edges) {
        edges.forEach(this::parse);

        // Check existence of method "main" and that every method has an entry point.
        if ( mainEntryNode == null ) System.err.println("No method 'main' declared.");
        for ( String method : methods )
            if ( !methodEntryPoints.containsKey(method) )
                System.err.println("Method '" +method+ "' does not have an entry point.");
    }


    ////////////////////////////////////////////////////
    //                   toGraphviz                   //
    ////////////////////////////////////////////////////
    public String toGVstring() {
        StringBuilder str = new StringBuilder("digraph flow_graph {");
        str.append(format("%nsize=\"19,11\"%n"));

        // Make return nodes double circles
        str.append(format("node [shape=doublecircle];%n"));
        for ( String node: returnNodes )
            str.append(format("\"%s\"; ", node));
        str.append(line);

        // Add entry point arrows
        StringBuilder startArrows = new StringBuilder();
        str.append(format("node [shape=point];%n"));
        for ( String method: methods ) {
            String entry = methodEntryPoints.get(method);
            str.append(format("\"%s_entry_indicator\"; ", entry));
            startArrows.append(format("\"%s_entry_indicator\"->\"%s\" [label=\"%s\"];%n", entry, entry, method));
        }

        str.append(format("%nnode [shape=circle];%n"));  // default node shape.
        str.append(startArrows);
        edges.mmmap.forEach((String q0, Map<String, Set<String>> vq1s) -> vq1s.forEach((String v, Set<String> q1s) -> {
            q1s.forEach(q1 -> str.append(format("\"%s\"->\"%s\" [label=\"%s\"];%n", q0, q1, v.equals("eps")?"ε":v)));
        }));
        return str.append("}").toString();
    }

    ////////////////////////////////////////////////////
    //                    toString                    //
    ////////////////////////////////////////////////////
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for ( String node : nodes ) {
            str.append(format("node %s meth(%s)", node, nodeMethods.get(node)));
            if ( returnNodes.contains(node) ) str.append(" ret");
            if ( methodEntryPoints.get(nodeMethods.get(node)).equals(node) ) str.append(" entry");
            str.append(line);
        }
        edges.mmmap.forEach((String q0, Map<String, Set<String>> vq1s) -> vq1s.forEach((String v, Set<String> q1s) -> {
            q1s.forEach(q1 -> str.append(format("edge %s %s %s%s", q0, q1, v.equals("eps")?"ε":v, line)));
        }));
        return str.toString();
    }

    ////////////////////////////////////////////////////
    //                    parsing                     //
    ////////////////////////////////////////////////////
    // Parse using recursive descent
    private void parse(String edgeStr) {
        edgeStr = edgeStr.replaceAll("\\s+", " ");
        String[] tmp = edgeStr.split(" ");

        // Turn edgeStr into Queue of Characters.
        Queue<String> words = new LinkedList<>();
        words.addAll(Arrays.asList(tmp));

        // Empty edge ok, just doesn't do anything
        if ( words.isEmpty() ) return;

        try {
            String next = removeAndCheckNonempty(words);
            switch (next) {
                case "node":
                    parseNode(words);
                    break;
                case "edge":
                    parseEdge(words);
                    break;
                default:
                    throw new ParseException("Expected 'node' or 'edge', got '" + next + "'.", 0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println(format("Error in '%s'. %s", edgeStr, e.getMessage()));
        }
    }

    private void parseEdge(Queue<String> words) throws ParseException {
        String v0 = removeAndCheckNonempty(words);
        checkLabelOk(v0);
        String v1 = removeAndCheckNonempty(words);
        checkLabelOk(v1);
        String method = removeAndCheckNonempty(words);
        checkLabelOk(method);
        boolean v0Exists = nodes.contains(v0);
        boolean v1Exists = nodes.contains(v1);
        if ( !v0Exists || !v1Exists ) {
            String missingNode = v0Exists ? v1 : v0;
            String err_msg = "Undeclared node '" + missingNode + "' in edge.";
            throw new ParseException(err_msg, 0);
        }
        if ( !nodeMethods.get(v0).equals(nodeMethods.get(v1)) ) {
            String err_msg_f = "Nodes %s (method %s) and %s (method %s) must belong to the same method";
            String err_msg = format(err_msg_f, v0, nodeMethods.get(v0), v1, nodeMethods.get(v1));
            throw new ParseException(err_msg, 0);
        }
        if ( !method.equals("eps") && !methods.contains(method) )
            throw new ParseException("Undeclared method name '"+method+"' in edge.", 0);
        checkWordQueueEmpty(words);
        edges.put(v0, method, v1);
    }

    private void parseNode(Queue<String> words) throws ParseException {
        String label = removeAndCheckNonempty(words);
        checkLabelOk(label);
        String method = parseMethod(removeAndCheckNonempty(words));
        String type = words.poll();  // Empty type is ok, thus poll().
        if ( type == null ) type = "";
        if ( !( type.equals("") || type.equals("entry") || type.equals("ret") ) ) {
            String err_msg = "Illegal node type. Expected 'entry' or 'ret' or no type at all. Got '"
                    + type + "'.";
            throw new ParseException(err_msg, 0);
        }
        if ( nodes.contains(label) ) {
            String err_msg = "Repeated declaration of node '"+label+"'.";
            throw new ParseException(err_msg, 0);
        }
        checkWordQueueEmpty(words);
        nodes.add(label);
        methods.add(method);
        nodeMethods.put(label, method);
        if ( type.equals("ret") ) returnNodes.add(label);
        else if ( type.equals("entry") ) {
            methodEntryPoints.put(method, label);
            if ( method.equals("main") ) mainEntryNode = label;
        }
    }

    private static String parseMethod(String methDecl) throws ParseException {
        if ( !methDecl.matches("meth\\(.+\\)") ) {
            String err_msg = "Unexpected method declaration. Expected 'meth(<methodName>)'. Got '"
                    +methDecl+"'.";
            throw new ParseException(err_msg, 0);
        }
        String methLabel = methDecl.substring(5, methDecl.length()-1);
        checkLabelOk(methLabel);
        return methLabel;
    }

    // Removes and returns next String in Queue. If there is no next, throw exception.
    private static String removeAndCheckNonempty(Queue<String> strs) throws ParseException {
        if (strs.isEmpty()) throw new ParseException("Unexpected end of string", 0);
        return strs.remove();
    }

    private static void checkWordQueueEmpty(Queue<String> words) throws ParseException {
        if ( !words.isEmpty() ) {
            String err_msg = "Unexpected text '"+words.remove()+"' at end of line.";
            throw new ParseException(err_msg, 0);
        }
    }

    private static void checkLabelOk(String label) throws ParseException {
        if ( label.equals("node") || label.equals("edge") || label.equals("meth") || label
                .equals("entry") || label.equals("ret") || label.equals("") ) {
            throw new ParseException("Illegal label '"+label+"'.", 0);
        }
        if ( label.contains("(") || label.contains(")") )
            throw new ParseException("Illegal label character '(' or ')' in '"+label+"'", 0);
    }

}

import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.String.format;

public class FG {
    private static final String line = System.lineSeparator();

    // HashMap requires the key to override hashCode and equals if we want
    // to map on something other than memory address.
    public Set<DFAedge> edges = new HashSet<>();
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
        str.append(line);
        str.append("size=\"19,11\"");
//        str.append("rankdir=LR; size=\"19,11\"");
        str.append(line);
        for ( String node: returnNodes )  // Make return nodes double circles
            str.append(format("node [shape=doublecircle]; %s;%s", node, line));
        StringBuilder startArrows = new StringBuilder();
        for ( String method: methods ) {  // Add entry point arrows
            String entry = methodEntryPoints.get(method);
            str.append(format("node [shape=point]; %s_entry_indicator;%s", entry, line));
            startArrows.append(format("%s_entry_indicator->%s;%s", entry, entry, line));
        }
        str.append(startArrows);
        for ( DFAedge e: edges )
            str.append(format("%s->%s [label=\"%s\"];%s", e.q0, e.q1, e.v.equals("eps")?"ε":e.v, line));
        str.setLength( str.length()-1 );  // Remove last newline.
        return str.toString();
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
        for ( DFAedge edge : edges ) {
            str.append(format("edge %s %s %s%s", edge.q0, edge.q1, edge.v.equals("eps")?"ε":edge.v, line));
        }
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
        for (String str : tmp) words.add(str);

        // Empty edge ok, just doesn't do anything
        if ( words.isEmpty() ) return;

        boolean dbTmp = false;  // db
        try {
            String next = removeAndCheckNonempty(words);
            if (next.equals("node")) parseNode(words);
            else if (next.equals("edge")) parseEdge(words);
            else throw new ParseException("Expected 'node' or 'edge', got '" + next + "'.", 0);
        } catch (ParseException e) {
            if ( !dbTmp ) {
                for (String methName : methods) System.err.println(methName);
                dbTmp = true;
            }
            e.printStackTrace();
            System.err.println(format("Error in '%s'. %s", edgeStr, e.getMessage()));
            return;
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
        edges.add(new DFAedge(v0, method, v1));
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
            String err_msg = "Illegal label '"+label+"'.";
            throw new ParseException("Illegal label '"+label+"'.", 0);
        }
        if ( label.contains("(") || label.contains(")") )
            throw new ParseException("Illegal label character '(' or ')' in '"+label+"'", 0);
    }

}

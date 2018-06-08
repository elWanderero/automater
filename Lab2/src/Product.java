import java.util.*;

public class Product {

    public static class Result {
        public final boolean generating;
        public final List<String> strs;

        public Result(boolean generating, List<String> generatedStrings) {
            this.generating = generating;
            this.strs = generatedStrings;
        }

        public Result(boolean generating, String str) {
            this.generating = generating;
            this.strs = new LinkedList<>();
            if ( str.length()>=1 ) strs.add(str);
        }

        public Result() {
            generating = false;
            strs = null;
        }

        @Override
        public String toString() {
            StringBuilder strb = new StringBuilder("Result{ generating=" + generating);
            if (generating) {
                strb.append(", String =");
                strs.forEach( str -> strb.append(" ").append(str));
            }
            strb.append(" }");
            return strb.toString();
        }
    }

    private static Result checkMe(DFAedge me, DFA dfa, FG fg, Set<DFAedge> ancestors, Map<DFAedge, Result>
            alreadyChecked) {

        if ( alreadyChecked.containsKey(me) ) return alreadyChecked.get(me);

        // If we have looped around, the root accepts iff only some other branch generates.
        // Alternatively this branch is already being evaluated, so we cut off here.
        if ( ancestors.contains(me) ) return new Result();

        // Else evaluate for realsies

        Result result = new Result();

        String q0 = me.q0;
        String v = me.v;
        String q1 = me.q1;

        // Order of the tests are so that we resolve as quickly as possible.
        // 4.
        if ( q0.equals(q1) && fg.returnNodes.contains(v) ) {
            result = new Result(true, "");
        }
        // 5.
        else if ( dfa.allEdges.contains(me) ) {
            result = new Result(true, v);
        } else {
            ancestors.add(me);
            // 2. Transfer edges before call edges, since transfer edges takes us closer to the return.
            for (String v1 : fg.edges.get(v, "eps")) {
                if (result.generating) break;
                result = checkMe(new DFAedge(q0, v1, q1), dfa, fg, ancestors, alreadyChecked);
            }
            // 3.
            Set<String> methodCalls = fg.edges.get(v).keySet();
            // For every call edge v -a-> v1, for all
            // [q0 v q1] -> [q0 a qj][qj a_entry qk][qk v1 q1]
            findGenerator:
            for (String a : methodCalls) {
                if (a.equals("eps")) break;
                Set<DFAedge> aEdges = dfa.edgesByLabel.get(a);  // all [qi a qj]
                if (aEdges == null) break;
                for (DFAedge e1 : aEdges) {  // e1 = [qi a qj]
                    if (!e1.q0.equals(q0)) continue;  // Only look at e1 = [q0 a qj]
                    Result res1 = checkMe(e1, dfa, fg, ancestors, alreadyChecked);
                    if (!res1.generating) continue;  // Only go on if [q0 a qj] is generating
                    for ( String qk : dfa.allNodes ) {
                        Result res2 = checkMe(new DFAedge(e1.q1, fg.methodEntryPoints.get(a), qk), dfa, fg, ancestors,
                                alreadyChecked);
                        if (!res2.generating) continue;
                        Result res3 = new Result();
                        for ( String v1: fg.edges.get(v, a) ) {  // call edge v -a-> v1 => [qk v1 q1]
                            if (res3.generating) break;
                            res3 = checkMe(new DFAedge(qk, v1, q1), dfa, fg, ancestors, alreadyChecked);
                        }
                        if ( res3.generating ) {
                            List<String> newStrs = res1.strs;
                            newStrs.addAll(res2.strs);
                            newStrs.addAll(res3.strs);
                            result = new Result(true, newStrs);
                            break findGenerator;
                        }
                    }
                }

            }
        }

        ancestors.remove(me);
        alreadyChecked.put(me, result);
        return result;

    }

    // Lazy evaluation.
    public static Result gurovProduct(DFA dfa, FG fg) {
        Map<DFAedge, Result> alreadyChecked = new HashMap<>();

        for ( String acceptNode : dfa.acceptingNodes ) {
            DFAedge root = new DFAedge(dfa.start, fg.mainEntryNode, acceptNode);
            Result result = checkMe(root, dfa, fg, new HashSet<>(), alreadyChecked);
            if ( result.generating ) return result;
        }
        return new Result();
    }

}

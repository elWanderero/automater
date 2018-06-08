/* DD2372 Automata and Languages, vt2018
 * Lab assignment 2: Model Checking Flow Graphs.
 * Author: Daniel Zavala-Svensson
 * kth-id: daniels@kth.se
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws Exception {
        String dir = args.length == 0 ? "Editor" : args[0];

        File[] fgFiles = Utils.getFilesWithEnding(dir, ".cfg");
        File fgFile = fgFiles[fgFiles.length-1];
        File[] specs = Utils.getFilesWithEnding(dir, ".spec");

        BufferedReader input = new BufferedReader(new FileReader(fgFile));
        // Fix idiosyncrasies in the test cfg:s
        Stream<String> lines = input.lines().map( str -> Utils.fixWeirdAssFlowGraphs(str, dir) );

        FG flowGraph = new FG();
        flowGraph.addTransitions(lines);
        input.close();
        System.out.println(flowGraph.toString());
        Utils.gvToFile(flowGraph.toGVstring(), "flow_graph");

        List<DFA> dfas = new ArrayList<>(specs.length);
        for ( File spec : specs ) {
            DFA dfa = new DFA();
            input = new BufferedReader(new FileReader(spec));
            dfa.addTransitions(input.lines().map(str-> Utils.fixWeirdAssSpecs(str,dir)));
            input.close();
            dfas.add(dfa);
            System.out.print(dfa.toString());
            dfa.invert();
            Utils.gvToFile(dfa.toGVstring());
            System.out.println(Product.gurovProduct(dfa, flowGraph).toString());
            System.out.println();
        }

    }
}

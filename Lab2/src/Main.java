/* DD2372 Automata and Languages, vt2018
 * Lab assignment 2: Model Checking Flow Graphs.
 * Author: Daniel Zavala-Svensson
 * kth-id: daniels@kth.se
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {

    private static boolean thisIsWindows() {
        return System.getProperty("os.name").equals("Windows 10");
    }

    private static final String winTestsDirPath = "./Lab2/tests";
    private static final String linuxTestsDirPath = "../tests";
    private static BufferedReader testReader(String testFileName) throws FileNotFoundException {
        File dir = thisIsWindows() ? new File(winTestsDirPath) : new File(linuxTestsDirPath);
        File testFile = new File(dir, testFileName);
        return new BufferedReader(new FileReader(testFile));
    }

    private static File[] getFilesWithEnding(String dirName, String fileEnding) {
        File testsRoot = thisIsWindows() ? new File(winTestsDirPath) : new File(linuxTestsDirPath);
        File dir = new File(testsRoot, dirName);
        return dir.listFiles((dir1, filename) -> filename.endsWith(fileEnding));
    }
    private static final String winGraphsDirPath = "./Lab2/graphs";
    private static final String linuxGraphsDirPath = "../graphs";
    private static Writer newGraphFileWriter(String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        File dir = thisIsWindows() ? new File(winGraphsDirPath) : new File(linuxGraphsDirPath);
        File graphFile = new File(dir, graphName);
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(graphFile),
                        "utf-8"
                )
        );
    }
    private static final String winDotCommand = "dot"; //"\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot\"";
    private static final String linuxDotCommand = "dot";
    private static void runDot(String gvFilename, String svgFilename) throws IOException {
        String graphsDir = thisIsWindows() ? winGraphsDirPath +"/" : linuxGraphsDirPath +"/";
        String dotCommand = thisIsWindows() ? winDotCommand : linuxDotCommand;
        String cmd = dotCommand + " -T svg -o " + graphsDir + svgFilename + " " + graphsDir + gvFilename;
        // Run cmd an bind process to variable to get output.
        Process proc = Runtime.getRuntime().exec(cmd);
        (new BufferedReader(new InputStreamReader(proc.getInputStream()))).lines().forEach(System.out::println);
        (new BufferedReader(new InputStreamReader(proc.getErrorStream()))).lines().forEach(System.err::println);
    }

    private static int fileCounter = 1;
    // Takes a graphviz dot-compatible string, generates a .gv and then runs dot on it.
    @SuppressWarnings("Duplicates")
    private static void gvToFile(String gvStr) throws IOException {
        // Count lines in gvStr as a measure of size.
        Matcher matcher = Pattern.compile("\r\n|\r|\n").matcher(gvStr);
        int lineCount = 0;
        while (matcher.find()) lineCount++;
        // Dot runs fine up to a few thousand edges, but too big a graph is pointless
        if ( lineCount>500 ) {
            System.err.println("Graphviz file too big, dot aborted.");
            return;
        }

        String suffix = String.valueOf(fileCounter++);
        String gvFilename = "graph_" + suffix + ".gv";
        String svgFilename = "graph_" + suffix + ".svg";

        Writer writer = newGraphFileWriter(gvFilename);
        writer.write(gvStr);
        writer.close();
        runDot(gvFilename, svgFilename);
    }

    private static String fixWeirdAssFlowGraphs(String weirdAssFG, String prefix) {
        // Remove internal method prefixes
        String ret = weirdAssFG.replaceAll(prefix+"-", "");
        return ret.replaceAll("(edge\\s+[^\\s]+\\s+[^\\s]+)\\s+[^\\s]+-[^\\s]+", "$1 eps" );
    }

    private static String fixWeirdAssSpecs(String weirdAssFG, String prefix) {
        // Remove internal method prefixes
        String ret = weirdAssFG.replaceAll(prefix+"-", "");
        // Replace external calls with eps
        return ret.replaceAll("-java(-[^\\n]+)*->", "-eps->" );
    }

    public static void main(String[] args) throws Exception {
        String dir = args.length == 0 ? "EvenOdd" : args[0];

        File fgFile = getFilesWithEnding(dir, ".cfg")[0];
        File[] specs = getFilesWithEnding(dir, ".spec");

        BufferedReader input = new BufferedReader(new FileReader(fgFile));
        // Fix idiosyncrasies in the test cfg:s
        Stream<String> lines = input.lines().map( str -> fixWeirdAssFlowGraphs(str, dir) );

        FG flowGraph = new FG();
        flowGraph.addTransitions(lines);
        input.close();
        System.out.println(flowGraph.toString());
        gvToFile(flowGraph.toGVstring());

        List<DFA> dfas = new ArrayList<>(specs.length);
        for ( File spec : specs ) {
            DFA dfa = new DFA();
            input = new BufferedReader(new FileReader(spec));
            dfa.addTransitions(input.lines().map(str->fixWeirdAssSpecs(str,dir)));
            input.close();
            dfas.add(dfa);
            System.out.println(dfa.toString());
            System.out.println();
            gvToFile(dfa.toGVstring());
        }

    }
}

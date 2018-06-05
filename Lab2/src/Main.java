/* DD2372 Automata and Languages, vt2018
 * Lab assignment 2: Model Checking Flow Graphs.
 * Author: Daniel Zavala-Svensson
 * kth-id: daniels@kth.se
 */

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private static final String winDotCommand = "\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot\"";
    private static final String linuxDotCommand = "dot";
    private static void runDot(String gvFilename, String svgFilename) throws IOException {
        String graphsDir = thisIsWindows() ? winGraphsDirPath +"/" : linuxGraphsDirPath +"/";
        String dotCommand = thisIsWindows() ? winDotCommand : linuxDotCommand;
        Runtime.getRuntime().exec(dotCommand + " -T svg -o " + graphsDir + svgFilename + " " + graphsDir + gvFilename);
    }

    private static int fileCounter = 1;
    // Takes a graphviz dot-compatible string, generates a .gv and then runs dot on it.
    private static void gvToFile(String gvStr) throws IOException {
        // Count lines in gvStr as a measure of size. Ironically we do this with regex
        // so with just some extra functionality we could do it with this very package.
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

    public static void main(String[] args) throws Exception {
        String dir = args.length == 0 ? "EvenOdd" : args[0];

        File fgFile = getFilesWithEnding(dir, ".cfg")[0];
        File[] specs = getFilesWithEnding(dir, ".spec");

        FG flowGraph = new FG();
        BufferedReader input = new BufferedReader(new FileReader(fgFile));
        Stream<String> lines = input.lines();
        // Remove internal method prefixes
        lines = lines.map( str -> str.replaceAll(dir+"-", ""));
        // Replace external method calls with eps.
        lines = lines.map( str -> str.replaceAll("(edge\\s+[^\\s]+\\s+[^\\s]+)\\s+[^\\s]+-[^\\s]+", "$1 eps" ));
        flowGraph.addTransitions(lines);
        input.close();
        System.out.println(flowGraph.toString());

//        List<DFA> dfas = new ArrayList<>(specs.length);
//        for ( File spec : specs ) {
//            DFA dfa = new DFA();
//            input = new BufferedReader(new FileReader(spec));
//            dfa.addTransitions(input.lines());
//            input.close();
//            dfas.add(dfa);
//            System.out.println(dfa.toString());
//            System.out.println();
//        }

//        char[] tmp = input.readLine().toCharArray();  // 1st line is alphabaet
//        Character[] alphabet = new Character[tmp.length];  // 2nd line is regex
//        for (int i=0 ; i<tmp.length ;++i) alphabet[i] = tmp[i];  // Cast chars to Characters
//
//
//        // Rest of input lines are evaluated with the DFA.
//        input.lines().forEachOrdered(
//                str -> {
//                    System.out.print(str);
//                    System.out.println(dfa.eval(str) ? " YES!" : " nope");
//                }
//        );
    }
}

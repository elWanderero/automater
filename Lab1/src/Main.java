/* DD2372 Automata and Languages, vt2018
 * Lab assignment 2: Model Checking Flow Graphs.
 * Author: Daniel Zavala-Svensson
 * kth-id: daniels@kth.se
 */

import java.io.*;

import DFAgraph.DFA;
import eNFAgraph.ENFA;
import nfa.NFA;
import resyntax.*;

public class Main {
    private static String dotCommand() {
        return thisIsWindows() ? "\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot\"" : "dot";
    }
    private static BufferedReader testReader(String testFileName) throws FileNotFoundException {
        File dir = thisIsWindows() ? new File("./Lab1/tests") : new File( "../tests");
        File testFile = new File(dir, testFileName);
        return new BufferedReader(new FileReader(testFile));
    }
    private static Writer newGraphFileWriter(String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        File dir = thisIsWindows() ? new File("./Lab1/graphs") : new File("../graphs");
        File graphFile = new File(dir, graphName);
        Writer writer = new BufferedWriter(
                            new OutputStreamWriter(
                                new FileOutputStream(graphFile),
                            "utf-8"
                            )
                        );
        return writer;
    }
    
    private static int fileCounter = 1;

    // Takes a graphviz dot-compatible string, generates a .gv and then runs dot on it.
    private static void gvToFile(String str) throws IOException {
        String suffix = String.valueOf(fileCounter++);
        String gvFilename = "graph_" + suffix + ".gv";
        String svgFilename = "graph_" + suffix + ".svg";

        Writer writer = newGraphFileWriter(gvFilename);
        writer.write(str);
        writer.close();
        Runtime.getRuntime().exec(dotCommand() + " -T svg -o " + svgFilename + " " + gvFilename);
    }

    /* Does the entire regex -> e-NFA -> NFA -> DFA -> minimal DFA thingy.
     * minimise is a flag that tells it whether or not to minimise (obviously)
     * and searchEverySubstring tells it whether to look for any accepting
     * contiguous substring, or only allow matching of the entire string.
     */
    private static DFA compile(String regex, Character[] alphabet, boolean minimise, boolean searchEverySubstring) throws Exception {
        if (searchEverySubstring) regex = ".*(" + regex + ")";
        RegExp regexRoot = REParser.parse(regex);
        System.out.println(regexRoot.toString());
        System.out.println();

        ENFA eNFA = new ENFA(alphabet);
        regexRoot.toENFA(eNFA);
        eNFA.initiate();
        System.out.println(eNFA.toString());
        gvToFile(eNFA.toGVstring());

        NFA nfa = eNFA.toNFA();
        gvToFile(nfa.toGVstring());
        System.out.println(nfa.toString());
        System.out.println();

        DFA dfa = nfa.toDFA();
        gvToFile(dfa.toGVstring());
        System.out.println(dfa.toString());

        if (minimise) {
            dfa = dfa.minimise();
            gvToFile(dfa.toGVstring());
            System.out.println(dfa.toString(false));
        }

        dfa.strongEvaluation = !searchEverySubstring;

        return dfa;
    }

    private static boolean thisIsWindows() {
        return System.getProperty("os.name").equals("Windows 10");
    }

    public static void main(String[] args) throws Exception {
        String testFileName = args.length == 0 ? "case4.txt" : args[0];
        BufferedReader input = testReader(testFileName);
        char[] tmp = input.readLine().toCharArray();  // 1st line is alphabaet
        Character[] alphabet = new Character[tmp.length];  // 2nd line is regex
        for (int i=0 ; i<tmp.length ;++i) alphabet[i] = tmp[i];  // Cast chars to Characters

        DFA dfa = compile(input.readLine(), alphabet, true, true);

        // Rest of input lines are evaluated with the DFA.
        input.lines().forEachOrdered(
                str -> {
                    System.out.print(str);
                    System.out.println(dfa.eval(str) ? " YES!" : " nope");
                }
        );
    }
}

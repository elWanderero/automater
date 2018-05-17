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

    private static final String winTestsDirPath = "./Lab1/tests";
    private static final String linuxTestsDirPath = "../tests";
    private static BufferedReader testReader(String testFileName) throws FileNotFoundException {
        File dir = thisIsWindows() ? new File(winTestsDirPath) : new File(linuxTestsDirPath);
        File testFile = new File(dir, testFileName);
        return new BufferedReader(new FileReader(testFile));
    }

    private static final String winGraphsDirPath = "./Lab1/graphs";
    private static final String linuxGraphsDirPath = "../graphs";
    private static Writer newGraphFileWriter(String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        File dir = thisIsWindows() ? new File(winGraphsDirPath) : new File(linuxGraphsDirPath);
        File graphFile = new File(dir, graphName);
        Writer writer = new BufferedWriter(
                            new OutputStreamWriter(
                                new FileOutputStream(graphFile),
                            "utf-8"
                            )
                        );
        return writer;
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
        String suffix = String.valueOf(fileCounter++);
        String gvFilename = "graph_" + suffix + ".gv";
        String svgFilename = "graph_" + suffix + ".svg";

        Writer writer = newGraphFileWriter(gvFilename);
        writer.write(gvStr);
        writer.close();
        runDot(gvFilename, svgFilename);
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

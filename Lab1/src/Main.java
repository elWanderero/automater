import java.io.*;

import DFAgraph.DFA;
import eNFAgraph.ENFA;
import nfa.NFA;
import resyntax.*;

public class Main {

    private static int fileCounter = 1;

    private static void gvToFile(String str) throws IOException {
        String suffix = String.valueOf(fileCounter++);
        String gvFilename = "graph_" + suffix + ".gv";
        String svgFilename = "graph_" + suffix + ".svg";
        String dotBinPath = "\"C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot\"";

        Writer writer = new BufferedWriter(
                            new OutputStreamWriter(
                                new FileOutputStream(gvFilename),
                            "utf-8"
                            )
                        );
        writer.write(str);
        writer.close();
        Runtime.getRuntime().exec(dotBinPath + " -T svg -o " + svgFilename + " " + gvFilename);
    }

    private static DFA compile(String regex, Character[] alphabet, boolean minimise, boolean searchEverySubstring) throws Exception {
        if (searchEverySubstring) regex = ".*" + regex;
        RegExp regexRoot = REParser.parse(regex);
        System.out.println(regexRoot.toString());

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

    public static void main(String[] args) throws Exception {
        BufferedReader input = new BufferedReader(new FileReader("./Lab1/tests/testcase3.txt"));
        char[] tmp = input.readLine().toCharArray();
        Character[] alphabet = new Character[tmp.length];
        for (int i=0 ; i<tmp.length ;++i) alphabet[i] = tmp[i];
        DFA dfa = compile(input.readLine(), alphabet, true, false);

        input.lines().forEachOrdered(
                str -> {
                    System.out.print(str);
                    System.out.println(dfa.eval(str) ? " YES!" : " nope");
                }
        );
    }
}

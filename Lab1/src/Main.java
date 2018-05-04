import java.io.*;

import DFAgraph.DFA;
import eNFAgraph.ENFA;
import nfa.NFA;
import resyntax.*;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader input = new BufferedReader(new FileReader("./Lab1/tests/testcase4.txt"));
        char[] tmp = input.readLine().toCharArray();
        Character[] alphabet = new Character[tmp.length];
        for (int i=0 ; i<tmp.length ;++i) alphabet[i] = tmp[i];
        RegExp regexRoot = REParser.parse(input.readLine());
        System.out.println(regexRoot.toString());


        ENFA eNFA = new ENFA(alphabet);
        regexRoot.toENFA(eNFA);
        eNFA.acceptNode.accepting = true;
//        System.out.println(eNFA.toString());

        NFA nfa = eNFA.toNFA();
//        System.out.println(nfa.toString());
//        System.out.println();

        DFA dfa = nfa.toDFA();
//        System.out.println(dfa.toString());

        DFA minimalDFA = dfa.minimise();
//        System.out.println(minimalDFA.toString(false));

        DFA minimalDFA2 = minimalDFA.minimise();
        System.out.println(minimalDFA2.toString());

        input.lines().forEachOrdered(
                str -> {
                    System.out.print(str);
                    System.out.println(minimalDFA.eval(str) ? " YES!" : " nope");
                }
        );
    }
}

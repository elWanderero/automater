import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import eNFAgraph.ENFA;
import resyntax.*;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader input = new BufferedReader(new FileReader("./Lab1/tests/testcase2.txt"));

        char[] tmp = input.readLine().toCharArray();
        Character[] alphabet = new Character[tmp.length];
        for (int i=0 ; i<tmp.length ;++i) alphabet[i] = tmp[i];
        RegExp regexRoot = REParser.parse(input.readLine());
        System.out.println(regexRoot.toString());
        ENFA eNFA = new ENFA(alphabet);
        regexRoot.toENFA(eNFA);
        eNFA.acceptNode.accepting = true;

        System.out.println(eNFA.toString());
        eNFA.toNFA();

    }
}

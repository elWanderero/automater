/* DD2372 Automata and Languages, vt2018
 * Lab assignment 2: Model Checking Flow Graphs.
 * Author: Daniel Zavala-Svensson
 * kth-id: daniels@kth.se
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader("./Lab2/tests/simple.cfg"));
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

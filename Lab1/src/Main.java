import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader bar = new BufferedReader(new FileReader("./Lab1/tests/testcase1.txt"));
        char[] foo = bar.readLine().toCharArray();
        String unparsedRegex = (new StringBuilder(bar.readLine()).reverse().toString());
        System.out.println(unparsedRegex);
    }
}

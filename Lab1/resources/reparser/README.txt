If your current directory is myLab1, and your source code is located in the
directory myLab1, then copy the directory

/info/DD2372/automat18/lab1/src/automata

to myLab1 with the command (note the ending dot)

cp -r /info/DD2372/automat18/lab1/src/automata .

Your main file, Main.java, can then invoke the parser with
REParser.parse(regExpToParse), where regExpToParse is a String object
containing the regular expression to parse and with the return type being
RegExp (the implementation of RegExp is in the directory
myLab1/automata/resyntax).

The following source code example illustrates how the parser can be used
(ote that the automata package must be imported):

import automata.REParser;
import automata.resyntax.*;

public class Main {
	public static void main(String[] args) throws Exception {
		String regExpToParse = "a|b";
		RegExp e = REParser.parse(regExpToParse);
		if (e instanceof Union)
			System.out.println("Is a union.\n");
		else
			System.out.println("Is not a union.\n");
	}
}

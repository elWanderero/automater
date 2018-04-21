package nfa;

import java.util.Dictionary;
import java.util.SortedSet;

public class NFA {
    public boolean[] acceptingStates;
    public Integer start;
    public Dictionary<Character, SortedSet<Integer>>[] transitionFcn;
    public Character[] alphabet;

    public NFA(boolean[] acceptingStates,
               Integer start,
               Dictionary<Character, SortedSet<Integer>>[] transitionFcn,
               Character[] alphabet) {
        this.acceptingStates = acceptingStates;
        this.start = start;
        this.transitionFcn = transitionFcn;
        this.alphabet = alphabet;
    }

    public void toPrint() {
        for (int i=0 ; i<transitionFcn.length ; i++) {
            if ( !(transitionFcn[i]==null) ) {
                System.out.printf("%2d ||", i);
                for (Character c : alphabet) {
                    if (transitionFcn[i].get(c).size() > 0) {
                        System.out.printf(" %c", c);
                        for (Integer id : transitionFcn[i].get(c)) System.out.printf("-%2d", id);
                        System.out.print(" |");
                    }
                }
                System.out.println();
            }
        }
    }
}

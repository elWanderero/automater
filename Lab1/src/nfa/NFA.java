/* Implemented as a function table, a variable denoting the start state, and a list of the accepting states.
 *
 */

package nfa;

import java.util.Dictionary;
import java.util.SortedSet;



public class NFA {
    public boolean[] acceptingStates;
    public Integer start;
    public Dictionary<Character, SortedSet<Integer>>[] transitionFcn;
    public Character[] alphabet;

    public NFA(Character[] alphabet,
               Dictionary<Character, SortedSet<Integer>>[] fcnTable,
               Integer start,
               boolean[] acceptingStates) {
        this.acceptingStates = acceptingStates;
        this.start = start;
        this.transitionFcn = fcnTable;
        this.alphabet = alphabet;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i=0 ; i<transitionFcn.length ; i++) {
            if ( !(transitionFcn[i]==null) ) {
                String annotation = acceptingStates[i] ? "*" : "";
                if ( i==start ) annotation += "•";
                str.append(String.format("%2d%-2s||", i, annotation));
                for (Character c : alphabet) {
                    if (transitionFcn[i].get(c).size() > 0) {
                        str.append(String.format(" %c", c));
                        for (Integer id : transitionFcn[i].get(c)) str.append(String.format("-%2d", id));
                        str.append(" |");
                    }
                }
                str.append(System.lineSeparator());
            }
        }
        return str.toString();
    }
}

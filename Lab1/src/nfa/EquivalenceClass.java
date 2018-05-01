package nfa;


import java.util.Iterator;
import java.util.TreeSet;

public class EquivalenceClass extends TreeSet<Integer> implements Comparable<TreeSet<Integer>> {

    @Override
    public int compareTo(TreeSet<Integer> rhs) {
        Iterator<Integer> lhsIter = this.iterator();
        Iterator<Integer> rhsIter = rhs.iterator();
        while ( lhsIter.hasNext() && rhsIter.hasNext() ) {
            Integer lhsNum = lhsIter.next();
            Integer rhsNum = rhsIter.next();
            if (!lhsNum.equals(rhsNum)) return lhsNum<rhsNum ? -1 : 1;
        }
        if ( !lhsIter.hasNext() && ! rhsIter.hasNext() ) return 0;
        else return rhsIter.hasNext() ? -1 : 1;
    }

    public boolean equals(TreeSet<Integer> rhs) {
        Iterator<Integer> rhsIter = rhs.iterator();
        for (Integer lhsNum : this) {
            if ( !rhsIter.hasNext() ) return false;
            Integer rhsNum = rhsIter.next();
            if (!lhsNum.equals(rhsNum)) return false;
        }
        return !rhsIter.hasNext();
    }



}

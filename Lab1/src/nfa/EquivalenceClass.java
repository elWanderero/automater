package nfa;


import java.util.Iterator;
import java.util.TreeSet;

/* For keeping track of equivalence classes. TreeSet is nice because it is a set, so automatically
 * doesn't allow duplicates. It is also a Navigable so that we can traverse it and do iterative
 * stuff with ease. It is also a Comparable, ordered lexicographically (possible since TreeSet is
 * also an OrderedSet), so that we can in turn create Sets of EquivalenceClass. */
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

}

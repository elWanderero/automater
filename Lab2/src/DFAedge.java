import java.util.Objects;

// Comparable so that we can sort collections of DFAedge, and search them faster.
public class DFAedge implements Comparable<DFAedge> {

    public String q0;
    public String v;
    public String q1;

    public DFAedge(String q0, String v, String q1) {
        this.q0 = q0;
        this.v = v;
        this.q1 = q1;
    }

    //  For efficient inclusion on structures. (Maybe not even needed if we use HashMaps!)
    @Override
    public int compareTo(DFAedge e2) {
        if ( !q0.equals(e2.q0) ) return q0.compareTo(e2.q0);
        if ( !v.equals(e2.v) ) return v.compareTo(e2.v);
        if ( !q1.equals(e2.q1) ) return q1.compareTo(e2.q1);
        return 0;
    }

    //  So that we can compare on content not object identity.
    public boolean equals(DFAedge e2) { return q0.equals(e2.q0) && v.equals(e2.v) && q1.equals(e2.q1); }

    //  So that DFAedges with equal contents get the same hash. This is necessary for
    //  my usage of maps etc. to work as intended.
    @Override
    public int hashCode() {
        return Objects.hash(q0, v, q1);
    }

}

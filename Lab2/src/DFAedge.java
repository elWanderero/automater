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

    @Override
    public int compareTo(DFAedge e2) {
        if ( !q0.equals(e2.q0) ) return q0.compareTo(e2.q0);
        if ( !v.equals(e2.v) ) return v.compareTo(e2.v);
        if ( !q1.equals(e2.q1) ) return q1.compareTo(e2.q1);
        return 0;
    }
}

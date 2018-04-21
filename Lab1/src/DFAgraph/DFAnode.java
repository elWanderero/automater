package DFAgraph;

import java.util.Dictionary;

public class DFAnode {
    public boolean accepting;
    public Dictionary<Character, DFAnode> edges;
}

package DFAgraph;

import java.util.Queue;

public class DeathNode extends DFAnode {

    DeathNode(Character[] alphabet, int id) {
        super(alphabet, id);
    }

    @Override
    boolean strongEval(String str, int index) {
        return false;
    }
}

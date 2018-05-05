package DFAgraph;

public class DeathNode extends DFAnode {

    DeathNode(Character[] alphabet, int id) {
        super(alphabet, id);
    }

    @Override
    boolean strongEval(String str, int index) {
        return false;
    }

    @Override
    void addToGVstringBuilder(StringBuilder str) {
        str.append(String.format("%d [ style=filled, fillcolor = black ];%n", id));
    }
}

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MMMap<K, Q, V> {

    public Map<K, Map<Q, Set<V>>> mmmap = new HashMap<>() ;

    public boolean put(K q0, Q v, V q1) {
        if ( !mmmap.containsKey(q0) ) mmmap.put(q0, new HashMap<>());
        Map<Q, Set<V>> map = mmmap.get(q0);
        if ( !map.containsKey(v) ) map.put(v, new HashSet<>());
        return map.get(v).add(q1);
    }

    public Map<Q, Set<V>> get(K q0) {
        if ( !mmmap.containsKey(q0) ) mmmap.put(q0, new HashMap<>());
        return mmmap.get(q0);
    }

    public Set<V> get(K q0, Q v) {
        if ( !mmmap.containsKey(q0) ) mmmap.put(q0, new HashMap<>());
        Map<Q, Set<V>> map = mmmap.get(q0);
        if ( !map.containsKey(v) ) map.put(v, new HashSet<>());
        return map.get(v);
    }

}

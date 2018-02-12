package Utils;

import Types.JSType;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

public class SetOperations {


    // returns first \/ second
    public static <T> Collection<T> union(Collection<T> first, Collection<T> second) {

        // union is (first /\ second) + (first \ second) + (second \ first)
        Collection<T> fstNoSnd = diff(first, second);
        Collection<T> sndNoFst = diff(second, first);
        Collection<T> result = intersect(first, second);

        result.addAll(fstNoSnd);
        result.addAll(sndNoFst);

        return result;
    }

    // returns first /\ second
    public static <T> Collection<T> intersect(Collection<T> first, Collection<T> second) {

        Collection<T> result = new TreeSet<>();

        for (T t : first) {
            if (second.contains(t)) {
                result.add(t);
            }
        }

        return result;
    }

    // returns left \ right
    public static <T> Collection<T> diff(Collection<T> left, Collection<T> right) {
        Collection<T> result = new TreeSet<>();

        for (T t : right) {
            if (!left.contains(t)) {
                result.add(t);
            }
        }

        return result;
    }
}

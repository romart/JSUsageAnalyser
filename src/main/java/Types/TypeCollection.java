package Types;

import Utils.SetOperations;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.Consumer;

public class TypeCollection implements Iterable<JSType> {

    private static TypeCollection empty = new TypeCollection();

    private Collection<JSType> storage = new TreeSet<>();

    public TypeCollection() {}
    public TypeCollection(JSType type) {
        addType(type);
    }

    private TypeCollection(Collection<JSType> storage) {
        this.storage = storage;
    }

    static public TypeCollection getEmpty() { return empty; }

    public void addType(JSType type) {
        if (type != null &&
            type != JSType.getUndefinedType() &&
            type != JSType.getNullType() &&
            !hasType(type)) {
            storage.add(type);
        }
    }

    public void addTypes(TypeCollection other) {
        unionWith(other);
    }


    // set operations
    public TypeCollection intersect(TypeCollection other) {
        Collection<JSType> intersection = SetOperations.intersect(storage, other.storage);
        return new TypeCollection(intersection);
    }

    public boolean intersectWith(TypeCollection other) {
        Collection<JSType> intersection = SetOperations.intersect(storage, other.storage);
        boolean changed = storage.size() != intersection.size();
        storage = intersection;
        return changed;
    }

    public TypeCollection union(TypeCollection other) {
        Collection<JSType> united = SetOperations.union(storage, other.storage);
        return new TypeCollection(united);
    }

    public boolean unionWith(TypeCollection other) {
        Collection<JSType> united = SetOperations.union(storage, other.storage);
        boolean changed = storage.size() != united.size();
        storage = united;
        return changed;
    }

    // slices
    public boolean sliceDown(JSType type) {
        Collection<JSType> slice = computeDownSlice(type);

        boolean changed = storage.size() != slice.size();

        if (!slice.contains(type)) {
            slice.add(type);
        }
        storage = slice;

        return changed;
    }

    public boolean sliceUp(JSType type) {
        Collection<JSType> slice = computeUpSlice(type);

        boolean changed = storage.size() == slice.size();
        storage = slice;

        return changed;
    }

    public TypeCollection downSlice(JSType type) {
        Collection<JSType> slice = computeDownSlice(type);
        if (!slice.contains(type)) {
            slice.add(type);
        }

        return new TypeCollection(slice);
    }

    public TypeCollection upSlice(JSType type) {
        Collection<JSType> slice = computeUpSlice(type);

        return new TypeCollection(slice);
    }

    private Collection<JSType> computeDownSlice(JSType type) {
        Collection<JSType> slice = new TreeSet<>();

        for (JSType t : storage) {
            if (t.isSubtypeOf(type)) {
                slice.add(t);
            }
        }

        return slice;
    }

    private Collection<JSType> computeUpSlice(JSType type) {
        Collection<JSType> slice = new TreeSet<>();

        for (JSType t : storage) {
            if (!t.isSubtypeOf(type)) {
                slice.add(t);
            }
        }

        return slice;
    }

    // hierarchy operations
    public JSType getLCA() {

        JSType lca = null;

        for (JSType type : storage) {
            if (lca == null) {
                lca = type;
            } else {
                lca = lca.leastCommonType(type);
            }
        }

        return lca;
    }

    public boolean isDifferent(TypeCollection other) {
        if (size() != other.size()) return true;

        for (JSType type : storage) {
            if (!other.hasType(type)) {
                return true;
            }
        }

        return false;
    }

    // storage operations
    public boolean isEmpty() {
        return storage.isEmpty();
    }
    public int size() {
        return storage.size();
    }
    public void clear() {
        storage.clear();
    }
    public boolean hasType(JSType type) {
        return storage.contains(type);
    }

    // Iterable interface
    @Override
    public Iterator<JSType> iterator() {
        return storage.iterator();
    }

    @Override
    public void forEach(Consumer<? super JSType> action) {
        storage.forEach(action);
    }

    @Override
    public Spliterator<JSType> spliterator() {
        return storage.spliterator();
    }
}

package Types;

import cfg.Nodes.Node;

import java.util.*;

public class JSType implements Comparable<JSType> {

    public JSType getSuperType() {
        return superType;
    }

    JSType superType;
    static int idCounter = 0;
    final int typeID = idCounter++;

    static JSType objectType = new JSObjectType();
    static JSType primitiveType = new JSType("<Primitive>");
    static JSType booleanType = new JSType("<boolean>");
    static JSType stringType = new JSType("String");
    static JSType undefinedType = new JSNAType("<undefined>");
    static JSType nullType = new JSNAType("<null>");

    public static JSType getObjectType() {
        return objectType;
    }
    public static JSType getPrimitiveType() {
        return primitiveType;
    }
    public static JSType getBooleanType() {
        return booleanType;
    }
    public static JSType getStringType() {
        return stringType;
    }
    public static JSType getUndefinedType() { return undefinedType; }

    public static JSType getNullType() { return nullType; }
    Map<String, JSType> classProperties = new HashMap<>();
    Set<String> classPropertiesNames = new HashSet<>();

    Map<String, Object> properties = new HashMap<>();
    Set<String> propertiesNames = new HashSet<>();

    String name;

    Set<JSType> childTypes = new TreeSet<>();


    public Function getParentFunction() {
        return parentFunction;
    }

    Function parentFunction;

    public boolean isMethodProperty() {
        return isMethodProperty;
    }

    boolean isMethodProperty;

    public JSType(Function f) {
        this.superType = getObjectType();
        this.name = f.getName();
        this.parentFunction = f;
        f.setAssociatedType(this);
        if (!isMethodProperty()) {
            f.setHolderType(this);
        }
    }

    private JSType(String name) {
        this.name = name;
    }

    public void addClassProperty(String propertyName) {
        classPropertiesNames.add(propertyName);
    }

    public void registerClassPropertyType(String propertyName, JSType type) {
        classProperties.put(propertyName, type);
    }

    public void addProperty(String propertyName) {
        propertiesNames.add(propertyName);
    }

    public void registerPropertyType(String propertyName, Node node) {
        properties.put(propertyName, node);
    }

    public String getName() {
        return name;
    }

    public void setIsMethod() {
        isMethodProperty = true;
    }

    public void inheritFromType(JSType parentType) {
        this.superType = parentType;
        this.superType.addChildType(this);
    }

    public void copyFromType(JSType copyType) {
        this.superType = copyType.getSuperType();
        this.classProperties = new HashMap<>(copyType.classProperties);
        this.classPropertiesNames = new HashSet<>(copyType.classPropertiesNames);
    }

    public Set<JSType> getChildTypes() {
        return childTypes;
    }

    public void addChildType(JSType type) {
        if (!childTypes.contains(type)) {
            childTypes.add(type);
        }
    }

    public boolean isDerivedTypeOf(JSType other) {
        return other.isSubtypeOf(this);
    }

    public boolean isSubtypeOf(JSType other) {
        JSType current = this;

//        if (other == JSType.getUndefinedType()) return true;

        while (current != null) {
            if (current == other) return true;
            current = current.getSuperType();
        }

        return false;
    }

    public JSType leastCommonType(JSType t2) {
        JSType t1 = this;

        for (;;) {
            if (t1.isSubtypeOf(t2)) return t2;
            if (t2.isSubtypeOf(t1)) return t1;

            t1 = t1.getSuperType();
            t2 = t2.getSuperType();
        }
    }

    public JSType propertyType(String name) {
        if (classPropertiesNames.contains(name)) {
            return classProperties.get(name);
        }
        if (superType != null) {
            return superType.propertyType(name);
        }
        return getUndefinedType();
    }

    public boolean hasProperty(String s) {
        return classPropertiesNames.contains(s);
    }

    @Override
    public int compareTo(JSType o) {
        return this.typeID - o.typeID;
    }

    @Override
    public String toString() {
        if (superType == null) {
            return getName();
        }

        return getName() + "->" + getSuperType().toString();
    }


    static class JSObjectType extends JSType {

        public JSObjectType() {
            super("Object");
        }

        // Object type has any property
        @Override
        public boolean hasProperty(String s) {
            return true;
        }

        @Override
        public JSType propertyType(String s) {
            return getObjectType();
        }

        @Override
        public boolean isSubtypeOf(JSType type) {
            return this == type;
        }
    }

    static class JSNAType extends JSType {
        public JSNAType(String name) {
            super(name);
        }

        @Override
        public boolean hasProperty(String s) {
            return false;
        }

        @Override
        public JSType propertyType(String s) {
            return getUndefinedType();
        }

        @Override
        public boolean isSubtypeOf(JSType type) {
            return false;
        }
    }

    public static JSType mergeTypes(JSType t1, JSType t2) {

        if (t1 == t2) {
            return t1;
        }

        if (t1 == null || t1 == JSType.getUndefinedType() || t1 == JSType.getNullType()) {
            return JSType.getUndefinedType();
        }

        if (t2 == null || t2 == JSType.getUndefinedType() || t2 == JSType.getNullType()) {
            return JSType.getUndefinedType();
        }

        return t1.leastCommonType(t2);
    }

    public boolean isSubtypeOfAny(TypeCollection types) {
        for (JSType t : types) {
            if (isSubtypeOf(t)) {
                return true;
            }
        }

        return false;
    }

}

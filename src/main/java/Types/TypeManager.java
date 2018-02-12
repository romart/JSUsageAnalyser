package Types;

import java.util.Map;
import java.util.TreeMap;

public class TypeManager {
    private static final Map<String, JSType> typeMap = new TreeMap<>();


    public static JSType getType(String name) {
        return typeMap.get(name);
    }

    public static boolean hasType(String name) {
        return typeMap.containsKey(name);
    }

    public static void inheritFromClass(String typeName, String superName) {
        JSType type = getType(typeName);
        JSType superType = getType(superName);

        if (type != null && superType != null) {
            type.inheritFromType(superType);
        }
    }

    public static void copyFromClass(String typeName, String parentName) {
        JSType type = getType(typeName);
        JSType parentType = getType(parentName);

        if (type != null && parentType != null) {
            type.copyFromType(parentType);
        }
    }

    public static void addPropertyToClass(String className, String propertyName, JSType propertyType) {
        JSType classType = getType(className);
        if (classType != null) {
            classType.registerClassPropertyType(propertyName, propertyType);
            classType.addClassProperty(propertyName);
        }
    }

    public static void registerNewType(String name, JSType type) {
        assert !hasType(name);

        typeMap.put(name, type);
    }
}


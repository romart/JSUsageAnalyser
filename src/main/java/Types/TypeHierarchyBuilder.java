package Types;

import cfg.*;
import cfg.Nodes.*;
import cfg.References.FuncReference;
import cfg.References.Property;
import cfg.References.Reference;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TypeHierarchyBuilder {

    private Map<String, JSType> types = new HashMap<>();

    public TypeHierarchyBuilder() {

        // initialize basic types
        FunctionManager.forEach(f -> {
            if (f != getMainFunction()) {
                TypeManager.registerNewType(f.getName(), new JSType(f));
            }
            return false;
        });
    }

    private static Function getMainFunction() { return FunctionManager.getMainFunction(); }

    public Map<String, JSType> getTypes() {
        return types;
    }

    public void process() {

       for (BasicBlock block : getMainFunction().getBlocks()) {
           LinkedList<Node> toRemove = new LinkedList<>();
           for (Node node : block.getNodes()) {
               if (node instanceof WritePropertyNode) {
                   if (processWritePropertyNode((WritePropertyNode) node)) {
                       toRemove.add(node);
                   }
               }
           }
           block.getNodes().removeAll(toRemove);
       }
    }

    private static final String PROTOTYPE = "prototype";
    private static final String OBJECT = "Object";
    private static final String CREATE = "create";

    private boolean processWritePropertyNode(WritePropertyNode node) {
        Property property = node.getProperty();
        Reference basePropRef = property.getBase();
        String propertyName = property.getName();
        Node source = node.getSourceNode();

        if (basePropRef instanceof Property) {

            Property baseRef = (Property) basePropRef;

            if (!(baseRef.getBase() instanceof FuncReference)) return false;

            if (baseRef.getName().equals(PROTOTYPE)) {
                FuncReference classRef = (FuncReference) baseRef.getBase();
                JSType type = TypeManager.getType(classRef.getName());

                type.addClassProperty(propertyName);

                if (source instanceof FunctionDeclarationNode) {
                    FunctionDeclarationNode fdNode = (FunctionDeclarationNode) source;
                    JSType propType = TypeManager.getType(fdNode.getFunction().getName());
                    propType.setIsMethod();
                    // used by This reference
                    fdNode.getFunction().setHolderType(type);
                    fdNode.getFunction().setAssociatedType(propType);
                    type.registerClassPropertyType(propertyName, propType);

                } else {
                    type.registerClassPropertyType(propertyName, JSType.getObjectType());
                }
                return true;
            } else { // .equals("prototype")
                throw new IllegalStateException("Unsupported semantic construction");
            }
        } else {
            if (basePropRef instanceof FuncReference) {
                FuncReference funcRef = (FuncReference) basePropRef;

                JSType type = TypeManager.getType(funcRef.getName());

                if (propertyName.equals(PROTOTYPE)) {
                    // probably it is B.prototype = ...
                    if (source instanceof CallNode) {
                        // it might be Object.create, lets check for that
                        JSType parentType = extractBaseType((CallNode) source);
                        if (parentType != null) {
                            type.inheritFromType(parentType);
                            return true;
                        } else {
                            throw new IllegalStateException("Unsupported semantic construction");
                        }
                    } else if (source instanceof ReadPropertyNode) {
                        // check whether it is B.prototype = A.prototype construction
                        // if so we should copy prototype's properties and superType from A to B
                        JSType copyType = extractBaseType((ReadPropertyNode) source);
                        if (copyType != null) {
                            type.copyFromType(copyType);
                            return true;
                        } else {
                            throw new IllegalStateException("Unsupported semantic construction");
                        }
                    } else {
                        throw new IllegalStateException("Unsupported semantic construction");
                    }
                } else { // .equals("prototype")
                    // so here might be anything like but the common pattern is A.field = ...
                    type.addProperty(propertyName);
                    type.registerPropertyType(propertyName, source);
                    return true;
                }
            }
        }
        return false;
    }

    // the two following methods are hardcoded pattern-matching
    private JSType extractBaseType(ReadPropertyNode source) {
        Property property = source.getProperty();
        String propertyName = property.getName();
        if (propertyName.equals(PROTOTYPE)) {
            if (property.getBase() instanceof FuncReference) {
                FuncReference funcRef = (FuncReference) property.getBase();
                return TypeManager.getType(funcRef.getName());
            }
        }

        return null;
    }

    private JSType extractBaseType(CallNode source) {

        if (source.getTargetReference() instanceof Property) {
            Property property = (Property)source.getTargetReference();
            if (property.getName().equals(CREATE)) {
                if (property.getBase() instanceof FuncReference) {
                    FuncReference funcRef = (FuncReference) property.getBase();
                    if (funcRef.getName().equals(OBJECT)) {
                        if (source.getParams().size() == 1) {
                            Node param = source.getParams().get(0);
                            if (param instanceof ReadPropertyNode) {
                                return extractBaseType((ReadPropertyNode) param);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }


}

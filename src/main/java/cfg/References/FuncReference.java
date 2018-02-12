package cfg.References;

import cfg.Nodes.*;

public class FuncReference extends Reference {

    private String funcName;

    public FuncReference(String name) {
        super(Type.FUNCTION);
        this.funcName = name;
    }

    @Override
    public String getName() {
        return funcName;
    }

    @Override
    public ReadReferenceNode asReadNode() {
        return new ReadFunctionReferenceNode(this);
    }

    @Override
    public WriteReferenceNode asWriteNode(Node source) {
        return null;
    }

    @Override
    public String toString() { return funcName; }

    public static FuncReference getObjectTypeReference() {
        return objectTypeReference;
    }

    static FuncReference objectTypeReference = new FuncReference("Object");


}

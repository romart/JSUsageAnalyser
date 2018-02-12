package cfg.References;

import cfg.Nodes.Node;
import cfg.Nodes.ReadReferenceNode;
import cfg.Nodes.WriteReferenceNode;

public abstract class Reference {

    public enum Type {
        VAR,
        PROPERTY,
        FUNCTION,
        THIS,
        EXTERNAL,
    }


    private Type type;

    public Type getType() {
        return type;
    }

    public abstract String getName();

    public abstract ReadReferenceNode  asReadNode();
    public abstract WriteReferenceNode asWriteNode(Node source);

    protected Reference(Type type) {
        this.type = type;
    }
}

package cfg.References;

import cfg.Nodes.*;

public class ThisReference extends Reference {
    public ThisReference() {
        super(Type.THIS);
    }


    @Override
    public String getName() {
        return "this";
    }

    @Override
    public ReadReferenceNode asReadNode() {
        return new ReadThisReferenceNode(this);
    }

    @Override
    public WriteReferenceNode asWriteNode(Node source) {
        return null;
    }

    @Override
    public String toString() { return getName(); }
}

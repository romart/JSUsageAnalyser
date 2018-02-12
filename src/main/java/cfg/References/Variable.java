package cfg.References;

import cfg.Nodes.*;

public class Variable extends Reference {

    private String varName;

    public Variable(String name) {
        super(Type.VAR);
        this.varName = name;
    }

    @Override
    public String getName() {
        return varName;
    }

    @Override
    public ReadReferenceNode asReadNode() {
        return new ReadVarNode(this);
    }

    @Override
    public WriteReferenceNode asWriteNode(Node source) {
        return new WriteVarNode(this, source);
    }

    @Override
    public String toString() { return varName; }
}

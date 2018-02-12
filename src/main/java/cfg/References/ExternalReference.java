package cfg.References;

import cfg.Nodes.*;

public class ExternalReference extends Reference {
    public ExternalReference(String name) {
        super(Type.EXTERNAL);
        this.name = name;
    }

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReadReferenceNode asReadNode() {
        return new ReadExReferenceNode(this);
    }

    @Override
    public WriteReferenceNode asWriteNode(Node source) {
        return new WriteExReferenceNode(this, source);
    }

    @Override
    public String toString() {
        return "$" + getName();
    }
}

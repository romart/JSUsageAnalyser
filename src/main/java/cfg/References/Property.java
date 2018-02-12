package cfg.References;

import cfg.Nodes.*;

public class Property extends Reference {


    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReadReferenceNode asReadNode() {
        return  new ReadPropertyNode(this);
    }

    @Override
    public WriteReferenceNode asWriteNode(Node source) {
        return new WritePropertyNode(this, source);
    }

    public Reference getBase() {
        return base;
    }

    private String name;
    private Reference base;

    public Property(String name, Reference base) {
        super(Type.PROPERTY);
        this.name = name;
        this.base = base;
    }

    @Override
    public String toString() {
        return base.toString() + "." + name;
    }
}

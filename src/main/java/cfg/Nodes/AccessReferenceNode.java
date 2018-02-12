package cfg.Nodes;

import cfg.References.Reference;

public abstract class AccessReferenceNode extends Node {
    Reference reference;

    public Reference getReference() {
        return reference;
    }

    protected AccessReferenceNode(Reference reference) {
        this.reference = reference;
    }
}

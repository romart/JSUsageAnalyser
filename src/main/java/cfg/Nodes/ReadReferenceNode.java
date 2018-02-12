package cfg.Nodes;

import cfg.References.Reference;

public abstract class ReadReferenceNode extends AccessReferenceNode {
    protected ReadReferenceNode(Reference reference) {
        super(reference);
    }
}

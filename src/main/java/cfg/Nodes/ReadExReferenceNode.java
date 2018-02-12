package cfg.Nodes;

import cfg.References.ExternalReference;
import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Reference;

public class ReadExReferenceNode extends ReadReferenceNode {

    public ReadExReferenceNode(Reference reference) {
        super(reference);
    }

    public ExternalReference getExternalReference() {
        return (ExternalReference) getReference();
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitReadExReferenceNode(this, p);
    }
}

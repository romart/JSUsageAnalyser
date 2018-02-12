package cfg.Nodes;

import cfg.References.ExternalReference;
import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Reference;

public class WriteExReferenceNode extends WriteReferenceNode {
    public WriteExReferenceNode(Reference reference, Node sourceNode) {
        super(reference, sourceNode);
    }

    public ExternalReference getExternalReference() {
        return (ExternalReference) getReference();
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitWriteExReferenceNode(this, p);
    }
}

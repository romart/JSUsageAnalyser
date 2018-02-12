package cfg.Nodes;

import cfg.References.ThisReference;
import visitors.CFGVisitors.NodeVisitor;

public class ReadThisReferenceNode extends ReadROReferenceNode {

    public ReadThisReferenceNode(ThisReference reference) {
        super(reference);
    }

    public ThisReference getThisReference() {
        return (ThisReference)getReference();
    }

    @Override
    public boolean isThisReference() {
        return true;
    }

    @Override
    public boolean isFuncReference() {
        return false;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitReadThisReference(this, p);
    }
}

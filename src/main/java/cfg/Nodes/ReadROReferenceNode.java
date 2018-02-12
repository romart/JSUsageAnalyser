package cfg.Nodes;

import cfg.References.FuncReference;
import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Reference;
import cfg.References.ThisReference;

// Synthetic node for using Read-Only references (This, Func) in CFG

public abstract class ReadROReferenceNode extends ReadReferenceNode {
    public abstract boolean isThisReference();
    public abstract boolean isFuncReference();

    public ThisReference getThisReference() { return (ThisReference) getReference(); }
    public FuncReference getFuncReference() { return (FuncReference) getReference(); }

    public ReadROReferenceNode(Reference reference) {
        super(reference);
    }

//    @Override
//    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
//        return visitor.visitReadROReference(this, p);
//    }
}

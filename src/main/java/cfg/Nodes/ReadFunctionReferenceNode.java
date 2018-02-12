package cfg.Nodes;

import cfg.References.FuncReference;
import visitors.CFGVisitors.NodeVisitor;

public class ReadFunctionReferenceNode extends ReadROReferenceNode {

    public ReadFunctionReferenceNode(FuncReference reference) {
        super(reference);
    }

    public FuncReference getFunctionReference() {
        return (FuncReference) getReference();
    }

    @Override
    public boolean isThisReference() {
        return false;
    }

    @Override
    public boolean isFuncReference() {
        return true;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitReadFuncReference(this, p);
    }
}

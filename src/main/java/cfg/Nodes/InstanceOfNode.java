package cfg.Nodes;

import cfg.References.FuncReference;
import visitors.CFGVisitors.NodeVisitor;

public class InstanceOfNode extends Node {
    ReadReferenceNode checkReference;
    FuncReference typeRefernce;

    public ReadReferenceNode getCheckReference() {
        return checkReference;
    }

    public FuncReference getTypeRefernce() {
        return typeRefernce;
    }

    public InstanceOfNode(ReadReferenceNode check, FuncReference type) {
        this.checkReference = check;
        this.typeRefernce = type;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitInstanceOf(this, p);
    }
}

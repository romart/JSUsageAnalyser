package cfg.Nodes;

import cfg.References.FuncReference;
import visitors.CFGVisitors.NodeVisitor;

public class NewObjectNode extends Node {

    public FuncReference getClassReference() {
        return classReference;
    }

    FuncReference classReference;

    public NewObjectNode(FuncReference ref) {
        this.classReference = ref;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitNewObjNode(this, p);
    }
}

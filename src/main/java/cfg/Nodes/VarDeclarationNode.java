package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;

public class VarDeclarationNode extends Node {
    public VarDeclarationNode(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    String varName;


    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitVarDeclarationNode(this, p);
    }
}

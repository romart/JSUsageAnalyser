package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Variable;

public class WriteVarNode extends WriteReferenceNode {

    public WriteVarNode(Variable var, Node source) {
        super(var, source);
    }

    public Variable getVariable() {
        return (Variable) getReference();
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitWriteVarNode(this, p);
    }
}

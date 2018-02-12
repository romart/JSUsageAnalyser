package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Variable;

public class ReadVarNode extends ReadReferenceNode {

    public ReadVarNode(Variable var) {
        super(var);
    }

    public Variable getVariableRef() {
        return (Variable) getReference();
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitReadVarNode(this, p);
    }
}

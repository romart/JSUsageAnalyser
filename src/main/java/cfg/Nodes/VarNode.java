package cfg.Nodes;

import cfg.References.Variable;

public abstract class VarNode extends Node {

    protected VarNode(Variable var) {
        this.variable = var;
    }

    public Variable getVariable() {
        return variable;
    }

    Variable variable;
}

package cfg.Nodes;

import Types.Function;
import visitors.CFGVisitors.NodeVisitor;

public class FunctionDeclarationNode extends Node {

    Function function;

    public Function getFunction() {
        return function;
    }

    public FunctionDeclarationNode(Function func) {
        this.function = func;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitFunctionDeclaration(this, p);
    }
}

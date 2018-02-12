package cfg.Nodes;

import Types.Function;
import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Reference;

import java.util.LinkedList;
import java.util.List;

public class CallNode extends Node {

    public Reference getTargetReference() {
        return target.getReference();
    }

    public ReadReferenceNode getTarget() {
        return target;
    }

    ReadReferenceNode target;

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    Function function; // if known
    List<Node> params = new LinkedList<>();

    public CallNode(ReadReferenceNode target) {
        this.target = target;
    }


    public void addParam(Node p) {
        params.add(p);
    }

    public List<Node> getParams() {
        return params;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitCall(this, p);
    }
}

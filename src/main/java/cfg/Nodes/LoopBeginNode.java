package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;

public class LoopBeginNode extends Node {

    IfNode branchNode;

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return null;
    }
}

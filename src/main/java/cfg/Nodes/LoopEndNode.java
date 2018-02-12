package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;

public class LoopEndNode extends Node {


    LoopBeginNode beginNode;

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return null;
    }
}

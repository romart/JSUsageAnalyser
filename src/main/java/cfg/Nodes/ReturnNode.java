package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;

public class ReturnNode extends Node {


    public Node getReturnNode() {
        return returnNode;
    }

    // might bw null
    Node returnNode;

    public ReturnNode(Node result) {
        returnNode = result;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitReturnNode(this, p);
    }
}

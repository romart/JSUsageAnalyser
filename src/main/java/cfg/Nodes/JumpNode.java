package cfg.Nodes;

import cfg.BasicBlock;
import visitors.CFGVisitors.NodeVisitor;

public class JumpNode extends Node {
    BasicBlock target;

    public JumpNode(BasicBlock target) {
        this.target = target;
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitJumpNode(this, p);
    }
}

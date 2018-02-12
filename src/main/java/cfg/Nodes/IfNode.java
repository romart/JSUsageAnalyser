package cfg.Nodes;

import cfg.BasicBlock;
import visitors.CFGVisitors.NodeVisitor;

public class IfNode extends Node {
    Node condition;

    BasicBlock trueBlock;
    BasicBlock falseBlock;

    BasicBlock continueBlock;

    boolean isDiamond;

    public IfNode(Node condition, BasicBlock trueBlock, BasicBlock falseBlock) {
        this.condition = condition;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }

    public Node getCondition() {
        return condition;
    }

    public BasicBlock getTrueBlock() {
        return trueBlock;
    }
    public BasicBlock getFalseBlock() {
        return falseBlock;
    }

    public BasicBlock getContinueBlock() {
        return continueBlock;
    }
    public void setContinueBlock(BasicBlock continueBlock) {
        this.continueBlock = continueBlock;
    }

    public boolean isDiamond() { return isDiamond; }
    public void setDiamond(boolean v) { isDiamond = v; }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitIf(this, p);
    }
}

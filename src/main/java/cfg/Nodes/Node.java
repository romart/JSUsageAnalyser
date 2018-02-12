package cfg.Nodes;

import Utils.SourceLocation;
import cfg.BasicBlock;
import visitors.CFGVisitors.NodeVisitor;
import jdk.nashorn.api.tree.Tree;

public abstract class Node {


    BasicBlock block;

    int index;
    static int nodeCounter;

    protected Node() {
        index = nodeCounter++;
    }

    SourceLocation location;

    public Tree getOriginalTree() {
        return originalTree;
    }

    public void setOriginalTree(Tree originalTree) {
        this.originalTree = originalTree;
    }

    Tree originalTree;

    public BasicBlock getBlock() {
        return block;
    }

    public void setBlock(BasicBlock block) {
        this.block = block;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public void setLocation(SourceLocation location) {
        this.location = location;
    }


    public abstract <R, P> R visit(NodeVisitor<R, P> visitor, P p);
}

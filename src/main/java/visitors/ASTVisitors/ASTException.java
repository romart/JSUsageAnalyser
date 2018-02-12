package visitors.ASTVisitors;

import Types.FunctionManager;
import Utils.SourceLocation;
import jdk.nashorn.api.tree.Tree;

public abstract class ASTException extends Error {

    // Triggered sub-tree
    Tree tree;
    SourceLocation location;

    protected ASTException(Tree tree, String message) {
        super(message);
        this.tree = tree;
        this.location = FunctionManager.makeSourceLocation(tree.getStartPosition());
    }

    public Tree getTree() {
        return tree;
    }

    public SourceLocation getLocation() {
        return location;
    }
}

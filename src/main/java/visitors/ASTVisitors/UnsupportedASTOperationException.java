package visitors.ASTVisitors;

import jdk.nashorn.api.tree.Tree;

public class UnsupportedASTOperationException extends ASTException {
    public UnsupportedASTOperationException(Tree tree) {
        super(tree, "Unsupported AST operation");
    }
}

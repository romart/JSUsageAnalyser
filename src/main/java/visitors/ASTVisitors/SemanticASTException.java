package visitors.ASTVisitors;

import jdk.nashorn.api.tree.Tree;

public class SemanticASTException extends ASTException {
    SemanticASTException(Tree tree, String message) {
        super(tree, message);
    }

}

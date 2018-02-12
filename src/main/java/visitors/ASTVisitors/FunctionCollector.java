package visitors.ASTVisitors;

import Types.FunctionManager;
import jdk.nashorn.api.tree.*;

public class FunctionCollector extends SimpleTreeVisitorES5_1<Void, Void> {


    @Override
    public Void visitFunctionDeclaration(final FunctionDeclarationTree node, final Void r) {

        IdentifierTree idTree = node.getName();
        FunctionManager.registerFunction(idTree.getName(), node, false);

        return null;
    }


    int anonFuncCounter;
    String createFunctionName() {
        return "function$$" + (anonFuncCounter++);
    }

    @Override
    public Void visitFunctionExpression(final FunctionExpressionTree node, final Void r) {

        IdentifierTree idTree = node.getName();
        String name = idTree != null ? idTree.getName() : createFunctionName();
        FunctionManager.registerFunction(name, node, true);

        return null;
    }
}

package visitors.ASTVisitors;

import jdk.nashorn.api.tree.*;

public abstract class ASTVisitor<R, P> implements TreeVisitor<R, P> {


    @Override
    public R visitArrayAccess(ArrayAccessTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitArrayLiteral(ArrayLiteralTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitObjectLiteral(ObjectLiteralTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitProperty(PropertyTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitRegExpLiteral(RegExpLiteralTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitTemplateLiteral(TemplateLiteralTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitEmptyStatement(EmptyStatementTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitSpread(SpreadTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitSwitch(SwitchTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitThrow(ThrowTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitModule(ModuleTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitExportEntry(ExportEntryTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitImportEntry(ImportEntryTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitTry(TryTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitWith(WithTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitYield(YieldTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitUnknown(Tree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitCase(CaseTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitCatch(CatchTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitClassDeclaration(ClassDeclarationTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitClassExpression(ClassExpressionTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitConditionalExpression(ConditionalExpressionTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitDebugger(DebuggerTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitDoWhileLoop(DoWhileLoopTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitErroneous(ErroneousTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitForLoop(ForLoopTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitForInLoop(ForInLoopTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitForOfLoop(ForOfLoopTree node, P p) {
        throw createUnsupportedException(node);
    }

    @Override
    public R visitBinary(BinaryTree node, P p) {
        throw createUnsupportedException(node);
    }

    private ASTException createUnsupportedException(Tree tree) {
        return new UnsupportedASTOperationException(tree);
    }
}

package visitors.ASTVisitors;

import Types.Function;
import Types.FunctionManager;
import Utils.SourceLocation;
import cfg.*;
import cfg.Nodes.*;
import cfg.References.*;
import jdk.nashorn.api.tree.*;

import java.util.*;

public class CFGBuilderVisitor extends ASTVisitor<BuildResult, BuildEnv> {

    private boolean isMain;

    private Stack<LoopBounds> loopStack = new Stack<>();

    private Map<StatementTree, String> labelMap = new HashMap<>();

    private class LoopBounds {
        private String loopLabel;

        private BasicBlock headBlock;
        private BasicBlock exitBlock;


        LoopBounds(BasicBlock head, BasicBlock exit, String label) {
            this.loopLabel = label;
            this.headBlock = head;
            this.exitBlock = exit;
        }

        private String     getLoopLabel() {
            return loopLabel;
        }
        private BasicBlock getHeadBlock() {
            return headBlock;
        }
        private BasicBlock getExitBlock() {
            return exitBlock;
        }

    }

    public CFGBuilderVisitor(boolean isMain) {
        this.isMain = isMain;
    }


    @Override
    public BuildResult visitFunctionDeclaration(final FunctionDeclarationTree node, final BuildEnv r) {

        if (!isMain) {
            proccessFunctionDeclaration(node.getBody(), node.getParameters(), r);
        }

        return null;
    }

    @Override
    public BuildResult visitFunctionExpression(FunctionExpressionTree node, BuildEnv buildEnv) {

        if (isMain) {

            Optional<Function> function = FunctionManager.getFunction(node);
            BuildResult res = new BuildResult(buildEnv.getAppendBlock());
            res.setRawResult(function.get().getDeclarationNode());
            return res;
        } else {
            proccessFunctionDeclaration(node.getBody(), node.getParameters(), buildEnv);
        }

        return null;
    }

    @Override
    public BuildResult visitIdentifier(IdentifierTree node, BuildEnv buildEnv) {
        String idName = node.getName();

        Function func = buildEnv.getFunction();
        Reference ref = null;
        Node nodeResult = null;
        if (idName.equals("undefined")) {
            nodeResult = ConstantNode.getUndefinedConstant();
        } else if(idName.equals("this")) {
            ref = func.getThisReference();
        } else if (idName.equals("Object")) {
            ref = FuncReference.getObjectTypeReference();
        } else if (func.hasVarName(idName)) {
            Variable vRef = new Variable(idName);
            nodeResult = new ReadVarNode(vRef);
            setLocation(nodeResult, node.getStartPosition());
            ref = vRef;
        } else if (FunctionManager.hasFuncName(idName)) {
            ref = new FuncReference(idName);
        } else {
            ref = new ExternalReference(idName);
        }


        return new BuildResult(nodeResult, ref, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitIf(IfTree node, BuildEnv buildEnv) {
        ExpressionTree condition = node.getCondition();
        StatementTree thenBranch = node.getThenStatement();
        StatementTree elseBranch = node.getElseStatement();

        BasicBlock currentBlock = buildEnv.getAppendBlock();
        Function function = buildEnv.getFunction();

        BuildResult condRes = condition.accept(this, buildEnv);
        BuildResult thenRes = null;
        BuildResult elseRes = null;

        // make sure we have explicit blocks for both then & else branches

        if (!(thenBranch instanceof BlockTree)) {
            BasicBlock thenBB = new BasicBlock(function);
            thenRes = thenBranch.accept(this, buildEnv.makeAppendEnv(thenBB));
            if (thenRes.getResult() != null) {
                thenBB.addNode(thenRes.getResult());
            }
        } else {
            thenRes = thenBranch.accept(this, buildEnv.makeAppendEnv(condRes.getLastBlock()));
        }

        if (elseBranch != null) {
            BasicBlock elseBB = condRes.getLastBlock();
            if (!(elseBranch instanceof BlockTree)) {
                // handle cases without explicit else block statement
                elseBB = new BasicBlock(function);
            }
            elseRes = elseBranch.accept(this, buildEnv.makeAppendEnv(elseBB));
            if (elseRes.getResult() != null) {
                elseBB.addNode(elseRes.getResult());
            }
        }

        BasicBlock continueBlock = new BasicBlock(function);
        BasicBlock trueBlock = thenRes.getFirstBlock();
        BasicBlock falseBlock = elseRes != null ? elseRes.getFirstBlock() : continueBlock;

        continueBlock.setEndLocation(currentBlock.getEndLocation());
        currentBlock.setEndLocation(trueBlock.getStartLocation());

        if (elseRes != null) {
            continueBlock.setStartLocation(elseRes.getLastBlock().getEndLocation());
        } else {
            continueBlock.setStartLocation(thenRes.getLastBlock().getEndLocation());
        }

        if (!thenRes.getLastBlock().isTerminated()) {
            // if it hasn't finished with break/continue
            thenRes.getLastBlock().addSuccessor(continueBlock);
            thenRes.getLastBlock().addNode(new JumpNode(continueBlock));
            thenRes.getLastBlock().setTerminated();
        }

        if (elseRes != null) {
            currentBlock.addSuccessor(elseRes.getFirstBlock());
            if (!elseRes.getLastBlock().isTerminated()) {
                elseRes.getLastBlock().addSuccessor(continueBlock);
                elseRes.getLastBlock().addNode(new JumpNode(continueBlock));
                elseRes.getLastBlock().setTerminated();
            }
        } else {
            currentBlock.addSuccessor(continueBlock);
        }

        currentBlock.addSuccessor(trueBlock);

        IfNode ifNode = new IfNode(condRes.getResult(), trueBlock, falseBlock);

        ifNode.setDiamond(elseRes != null);
        ifNode.setContinueBlock(continueBlock);
        setLocation(ifNode, node.getStartPosition());

        currentBlock.setTerminated();

        return new BuildResult(ifNode, currentBlock, continueBlock);
    }

    @Override
    public BuildResult visitLabeledStatement(LabeledStatementTree node, BuildEnv buildEnv) {
        labelMap.put(node.getStatement(), node.getLabel());
        return node.getStatement().accept(this, buildEnv);
    }

    @Override
    public BuildResult visitLiteral(LiteralTree node, BuildEnv buildEnv) {
        Object value = node.getValue();
        Tree.Kind kind = node.getKind();
        Node result;
        switch (kind) {
            case BOOLEAN_LITERAL:
                boolean val = (Boolean)value;
                result = val ? ConstantNode.getTrueConstant() : ConstantNode.getFalseConstant();
                break;

            case NULL_LITERAL:
                result = ConstantNode.getNullConstant();
                break;
            case NUMBER_LITERAL:
                if (value instanceof Integer) {
                    result = new ConstantNode((Integer)value);
                } else {
                    result = new ConstantNode((Double) value);
                }
                break;
            case STRING_LITERAL:
                result = new ConstantNode((String)value);
                break;
            default:
                throw new SemanticASTException(node, "Incorrect constant type");
        }

        setLocation(result, node.getStartPosition());

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitParenthesized(ParenthesizedTree node, BuildEnv buildEnv) {
        return null;
    }

    @Override
    public BuildResult visitReturn(ReturnTree node, BuildEnv buildEnv) {
        Node result = null;

        BasicBlock lastBLock = buildEnv.getAppendBlock();
        if (node.getExpression() != null) {
            BuildResult rRes = node.getExpression().accept(this, buildEnv);
            result = rRes.getResult();
        }
        lastBLock.setTerminated();

        ReturnNode retNode = new ReturnNode(result);
        setLocation(retNode, node.getStartPosition());

        return new BuildResult(retNode, lastBLock);
    }

    @Override
    public BuildResult visitMemberSelect(MemberSelectTree node, BuildEnv buildEnv) {

        String idName = node.getIdentifier();

        BuildResult baseRes = node.getExpression().accept(this, buildEnv);

        Property property = new Property(idName, baseRes.getResultReference());

        return new BuildResult(property, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitNew(NewTree node, BuildEnv buildEnv) {
        ExpressionTree ctorExpression = node.getConstructorExpression();
        BuildResult ctorRes = ctorExpression.accept(this, buildEnv);

        NewObjectNode result = new NewObjectNode((FuncReference) ctorRes.getResultReference());
        setLocation(result, node.getStartPosition());

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitCompilationUnit(CompilationUnitTree node, BuildEnv buildEnv) {
        BasicBlock.resetCounter();
        BasicBlock entryBB = new BasicBlock(buildEnv.getFunction());

        BuildResult res;
        BasicBlock currentBB = entryBB;

        for (Tree tree: node.getSourceElements()) {
            res = tree.accept(this, buildEnv.makeAppendEnv(currentBB));
            if (res == null) continue;
            if (res.getResult() != null) {
                currentBB.addNode(res.getResult());
            }
            currentBB = res.getLastBlock();
        }
        return new BuildResult(entryBB, currentBB);
    }

    @Override
    public BuildResult visitInstanceOf(InstanceOfTree node, BuildEnv buildEnv) {
        BuildResult exprRes = node.getExpression().accept(this, buildEnv);
        BuildResult typeRes = node.getType().accept(this, buildEnv);

        ReadReferenceNode expr = exprRes.getResultReference().asReadNode();
        setLocation(expr, node.getExpression().getStartPosition());

        InstanceOfNode result = new InstanceOfNode(expr, (FuncReference) typeRes.getResultReference());
        setLocation(result, node.getStartPosition());

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitUnary(UnaryTree node, BuildEnv buildEnv) {
        UnaryOpNode.Operation op = UnaryOpNode.treeKindToOperation(node.getKind());
        if (node.getKind() != Tree.Kind.LOGICAL_COMPLEMENT) {
            throw new UnsupportedASTOperationException(node);
        }
        BuildResult exprRes = node.getExpression().accept(this, buildEnv);

        UnaryOpNode result = new UnaryOpNode(op, exprRes.getResult());
        setLocation(result, node.getStartPosition());

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitVariable(VariableTree node, BuildEnv buildEnv) {
        IdentifierTree varName = (IdentifierTree) node.getBinding(); // has to be IdTree

        VarDeclarationNode declNode = new VarDeclarationNode(varName.getName());
        buildEnv.getAppendBlock().addNode(declNode);

        buildEnv.getFunction().addLocal(varName.getName());

        Variable var = new Variable(varName.getName());

        if (node.getInitializer() != null) {
            BuildResult initRes = node.getInitializer().accept(this, buildEnv);

            Node source = initRes.getResult();
            setLocation(source, node.getInitializer().getStartPosition());

            WriteReferenceNode wRefNode = var.asWriteNode(source);
            setLocation(wRefNode, node.getStartPosition());

            return new BuildResult(wRefNode, buildEnv.getAppendBlock());
        } else {
            return new BuildResult(buildEnv.getAppendBlock());
        }
    }

    @Override
    public BuildResult visitWhileLoop(WhileLoopTree node, BuildEnv buildEnv) {
        ExpressionTree cond = node.getCondition();
        StatementTree body = node.getStatement();

        Function function = buildEnv.getFunction();
        BasicBlock currentBB = buildEnv.getAppendBlock();
        BasicBlock headBB = new BasicBlock(function);
        BasicBlock continueBB = new BasicBlock(function);

        currentBB.addSuccessor(headBB);
        currentBB.setTerminated();

        pushLoop(headBB, continueBB, node);
        BuildResult condRes = cond.accept(this, buildEnv.makeAppendEnv(headBB));
        BuildResult bodyRes;
        if (body instanceof BlockTree) {
            bodyRes = body.accept(this, buildEnv.makeAppendEnv(condRes.getLastBlock()));
        } else {
            // handle cases without explicit block statement
            // like while (...) b.foo();
            BasicBlock bodyBB = new BasicBlock(function);
            bodyRes = body.accept(this, buildEnv.makeAppendEnv(bodyBB));
            if (bodyRes.getResult() != null) {
                bodyBB.addNode(bodyRes.getResult());
            }
        }
        popLoop();

//        condRes.getLastBlock().addSuccessor(headBB);
        headBB.addSuccessor(bodyRes.getFirstBlock());
        headBB.addSuccessor(continueBB);
        bodyRes.getLastBlock().addSuccessor(headBB);

        JumpNode backBranch = new JumpNode(headBB);
        bodyRes.getLastBlock().addNode(backBranch);
        bodyRes.getLastBlock().setTerminated();

        IfNode ifNode = new IfNode(condRes.getResult(), bodyRes.getFirstBlock(), continueBB);
//        headBB.addNode(condRes.getResult());
        headBB.addNode(ifNode);
        headBB.setTerminated();

        // fix up source location
        continueBB.setEndLocation(continueBB.getEndLocation());
        currentBB.setEndLocation(makeSourceLocation(cond.getStartPosition()));
        headBB.setStartLocation(currentBB.getEndLocation());
        headBB.setEndLocation(bodyRes.getFirstBlock().getStartLocation());
        continueBB.setStartLocation(bodyRes.getLastBlock().getEndLocation());

        ifNode.setDiamond(true);
        ifNode.setContinueBlock(continueBB);
        setLocation(ifNode, node.getStartPosition());

        return new BuildResult(currentBB, continueBB);
    }

    @Override
    public BuildResult visitAssignment(AssignmentTree node, BuildEnv buildEnv) {
        BuildResult varRes = node.getVariable().accept(this, buildEnv);
        BuildResult exprRes = node.getExpression().accept(this, buildEnv);

        Reference ref = varRes.getResultReference();

        if (ref == null) {
            throw new SemanticASTException(node, "Assignment is not into reference");
        }

//        Node source = node.getExpression().getKind() != Tree.Kind.FUNCTION_INVOCATION ? exprRes.getResult() : exprRes.getResult();
        Node source = exprRes.getResult();
        setLocation(source, node.getExpression().getStartPosition());

        Node result = varRes.getResultReference().asWriteNode(source);
        setLocation(result, node.getStartPosition());

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitCompoundAssignment(CompoundAssignmentTree node, BuildEnv buildEnv) {
        return null;
    }

    @Override
    public BuildResult visitBinary(BinaryTree node, BuildEnv buildEnv) {
        Tree.Kind kind = node.getKind();
        if (kind != Tree.Kind.EQUAL_TO && kind != Tree.Kind.NOT_EQUAL_TO) {
            throw new UnsupportedASTOperationException(node);
        }
        BinaryOpNode.Operation op = BinaryOpNode.treeKindToOperation(kind);

        BuildResult lRes = node.getLeftOperand().accept(this, buildEnv);
        BuildResult rRes = node.getRightOperand().accept(this, buildEnv);

        BinaryOpNode result = new BinaryOpNode(lRes.getResult(), op, rRes.getResult());
        setLocation(result, node.getStartPosition());

        return new BuildResult(result, buildEnv.getAppendBlock());

//        return null;
    }

    @Override
    public BuildResult visitBlock(BlockTree node, BuildEnv buildEnv) {
        BasicBlock newBlock = new BasicBlock(buildEnv.getFunction());

        newBlock.setStartLocation(makeSourceLocation(node.getStartPosition()));
        newBlock.setEndLocation(makeSourceLocation(node.getEndPosition()));

        buildEnv = buildEnv.makeAppendEnv(newBlock);
        BuildResult res;

        BasicBlock currentBlock = buildEnv.getAppendBlock();
        for (StatementTree tree : node.getStatements()) {
            res = tree.accept(this, buildEnv.makeAppendEnv(currentBlock));

            Node result = res.getResult();

            if (result != null) {
                setLocation(result, tree.getStartPosition());
                currentBlock.addNode(result);

            }
            currentBlock = res.getLastBlock();
        }

        return new BuildResult(newBlock, currentBlock);
    }

    @Override
    public BuildResult visitBreak(BreakTree node, BuildEnv buildEnv) {
        LoopBounds loop = getLoop(node);
        BasicBlock targetBB = loop.getExitBlock();

        JumpNode result = new JumpNode(targetBB);
        setLocation(result, node.getStartPosition());

        buildEnv.getAppendBlock().addSuccessor(targetBB);
        buildEnv.getAppendBlock().setTerminated();

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitContinue(ContinueTree node, BuildEnv buildEnv) {
        LoopBounds loop = getLoop(node);
        BasicBlock targetBB = loop.getHeadBlock();

        JumpNode result = new JumpNode(targetBB);
        setLocation(result, node.getStartPosition());

        buildEnv.getAppendBlock().addSuccessor(targetBB);
        buildEnv.getAppendBlock().setTerminated();

        return new BuildResult(result, buildEnv.getAppendBlock());
    }

    @Override
    public BuildResult visitExpressionStatement(ExpressionStatementTree node, BuildEnv buildEnv) {
        return node.getExpression().accept(this, buildEnv);
    }

    @Override
    public BuildResult visitFunctionCall(final FunctionCallTree callTree, final BuildEnv buildEnv) {
        BuildResult targetRes = callTree.getFunctionSelect().accept(this, buildEnv);

        ReadReferenceNode readReferenceNode = targetRes.getResultReference().asReadNode();
        setLocation(readReferenceNode, callTree.getFunctionSelect().getStartPosition());

        CallNode callNode = new CallNode(readReferenceNode);
        setLocation(callNode, callTree.getStartPosition());

        BuildResult pRes;

        for (ExpressionTree p : callTree.getArguments()) {
            pRes = p.accept(this, buildEnv);
            callNode.addParam(pRes.getResult());
        }


        return new BuildResult(callNode, targetRes.getResultReference(), buildEnv.getAppendBlock());
    }

    private FunctionDeclarationNode proccessFunctionDeclaration(Tree body, List<? extends ExpressionTree> params, BuildEnv env) {

        BasicBlock.resetCounter();

        Function func = env.getFunction();

        for (ExpressionTree p : params) {
            IdentifierTree id = (IdentifierTree)p;
            func.addParam(id.getName());
        }

        BuildResult res = body.accept(this, env);
        res.getLastBlock().setTerminated();

        FunctionDeclarationNode outNode = new FunctionDeclarationNode(func);
        func.setDeclarationNode(outNode);

        return outNode;
    }

    private LoopBounds getLoop(GotoTree node) {
        if (node.getLabel() == null) {
            return getNearestLoop();
        } else {
            Optional<LoopBounds> loop = getLabelLoop(node.getLabel());
            if (loop.isPresent()) {
                return loop.get();
            } else {
                throw new SemanticASTException(node, "No loop labeled with \"" + node.getLabel() + "\" found above");
            }
        }
    }

    private LoopBounds getNearestLoop() {
        return loopStack.peek();
    }

    private Optional<LoopBounds> getLabelLoop(String label) {
        return loopStack.stream().filter(l -> l.getLoopLabel() != null && l.getLoopLabel().equals(label)).findFirst();
    }

    private void pushLoop(BasicBlock headBB, BasicBlock continueBB, WhileLoopTree node) {
        String label = null;
        if (labelMap.containsKey(node)) {
            label = labelMap.get(node);
        }
        headBB.setLoopHead();
        loopStack.push(new LoopBounds(headBB, continueBB, label));
    }

    private void popLoop() {
        loopStack.pop();
    }

    private static void setLocation(Node node, long pos) {
        if (node.getLocation() != null) return;
        node.setLocation(makeSourceLocation(pos));
    }
    private static SourceLocation makeSourceLocation(long pos) {
        return FunctionManager.makeSourceLocation(pos);
    }
}

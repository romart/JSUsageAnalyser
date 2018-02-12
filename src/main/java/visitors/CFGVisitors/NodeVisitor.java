package visitors.CFGVisitors;

import cfg.Nodes.*;

public interface NodeVisitor<R, P> {


    R visitBinary(BinaryOpNode node, P p);
    R visitCall(CallNode node, P p);
    R visitConstant(ConstantNode node, P p);
    R visitFunctionDeclaration(FunctionDeclarationNode node, P p);
    R visitIf(IfNode node, P p);
    R visitInstanceOf(InstanceOfNode node, P p);
    R visitJumpNode(JumpNode node, P p);
    R visitNewObjNode(NewObjectNode node, P p);
    R visitReadPropertyNode(ReadPropertyNode node, P p);
    R visitReadThisReference(ReadThisReferenceNode node, P p);
    R visitReadFuncReference(ReadFunctionReferenceNode node, P p);
    R visitReadVarNode(ReadVarNode node, P p);
    R visitReadExReferenceNode(ReadExReferenceNode node, P p);
    R visitWriteExReferenceNode(WriteExReferenceNode node, P p);
    R visitReturnNode(ReturnNode node, P p);
    R visitUnaryNode(UnaryOpNode node, P p);
    R visitVarDeclarationNode(VarDeclarationNode node, P p);
    R visitWritePropertyNode(WritePropertyNode node, P p);
    R visitWriteVarNode(WriteVarNode node, P p);
}

package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;
import jdk.nashorn.api.tree.Tree;

public class UnaryOpNode extends Node {
    public enum Operation {
        MINUS, // +
        PLUS,  // -
        BNEG,  // ~
        LNEG,  // !

        PREINC, // ++i
        PREDEC, // --i
        PSTINT, // i++
        PSTDEC  // i--
    }

    Node expr;

    public Node getExpr() {
        return expr;
    }

    public Operation getOp() {
        return op;
    }

    Operation op;

    public UnaryOpNode(Operation op, Node expr) {
        this.op = op;
        this.expr = expr;
    }

    public static Operation treeKindToOperation(Tree.Kind kind) {
        switch (kind) {
            case UNARY_MINUS: return Operation.MINUS;
            case UNARY_PLUS: return Operation.PLUS;
            case BITWISE_COMPLEMENT: return Operation.BNEG;
            case LOGICAL_COMPLEMENT: return Operation.LNEG;

            case PREFIX_INCREMENT: return Operation.PREINC;
            case PREFIX_DECREMENT: return Operation.PREDEC;

            case POSTFIX_INCREMENT: return Operation.PSTINT;
            case POSTFIX_DECREMENT: return Operation.PSTDEC;

            default: throw new IllegalStateException("Unsupported unary operation: " + kind);
        }
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitUnaryNode(this, p);
    }
}

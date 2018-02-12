package cfg.Nodes;

import Types.JSType;
import visitors.CFGVisitors.NodeVisitor;
import jdk.nashorn.api.tree.Tree;

public class BinaryOpNode extends Node {
    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitBinary(this, p);
    }

    public enum Operation {
        // arith
        ADD,
        SUB,
        MUL,
        DIV,
        MOD,

        // logic
        AND,
        OR,
        XOR,

        // shifts
        SHL,
        SHR,
        USHR,

        // relations
        EQ,
        NE,
        LT,
        LE,
        GT,
        GE,

        // strict
        SEQ, // ===
        SNE, // !==

        // conditions
        CAND, // &&
        COR,  // ||
    }

    // left op right

    public Operation getOp() {
        return op;
    }

    public Node getRight() {
        return right;
    }

    public Node getLeft() {
        return left;
    }

    public JSType getResultType() {
        switch (getOp()) {
            case EQ:
            case NE:
            case LT:
            case LE:
            case GT:
            case GE:
                return JSType.getBooleanType();
            case ADD:
                // check for string concatenation
                return JSType.getPrimitiveType();
            default:
                return JSType.getPrimitiveType();
        }
    }

    Operation op;
    Node right, left;

    public BinaryOpNode(Node left, Operation op, Node right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }


    public static Operation treeKindToOperation(Tree.Kind kind) {
        switch (kind) {
            case PLUS:  return Operation.ADD;
            case MINUS: return Operation.SUB;
            case MULTIPLY: return Operation.MUL;
            case DIVIDE: return Operation.DIV;
            case MODULE: return Operation.MOD;

            case AND: return Operation.AND;
            case OR: return Operation.OR;
            case XOR: return Operation.XOR;

            case LEFT_SHIFT: return Operation.SHL;
            case RIGHT_SHIFT: return Operation.SHR;
            case UNSIGNED_RIGHT_SHIFT: return Operation.USHR;

            case EQUAL_TO: return Operation.EQ;
            case NOT_EQUAL_TO: return Operation.NE;
            case LESS_THAN: return Operation.LT;
            case LESS_THAN_EQUAL: return Operation.LE;
            case GREATER_THAN: return Operation.GT;
            case GREATER_THAN_EQUAL: return Operation.GE;

            case STRICT_EQUAL_TO: return Operation.SEQ;
            case STRICT_NOT_EQUAL_TO: return Operation.SNE;

            case CONDITIONAL_AND: return Operation.CAND;
            case CONDITIONAL_OR: return Operation.COR;

            default:
                throw new IllegalStateException("Incorrect kind of binary op");
        }
    }
}

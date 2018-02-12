package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;

public class ConstantNode extends Node {

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitConstant(this, p);
    }

    public enum  Type {
        BOOLEAN,
        INT_NUMBER,
        FLOAT_NUMBER,
        STRING,
        NULL,
        UNDEFINED
    }


    public ConstantNode(long intValue) {
        this.type = Type.INT_NUMBER;
        this.intValue = intValue;
    }

    public ConstantNode(double floatValue) {
        this.type = Type.FLOAT_NUMBER;
        this.floatValue = floatValue;
    }

    public ConstantNode(String stringValue) {
        this.type = Type.STRING;
        this.stringValue = stringValue;
    }

    private ConstantNode(Type type) {
        this.type = type;
    }

    private ConstantNode(boolean bool) {
        this.type = Type.BOOLEAN;
        this.boolValue = bool;
    }

    public static ConstantNode getNullConstant() {
        return nullConstant;
    }

    public static ConstantNode getUndefinedConstant() {
        return undefinedConstant;
    }

    public static ConstantNode getTrueConstant() {
        return trueConstant;
    }

    public static ConstantNode getFalseConstant() {
        return falseConstant;
    }

    static ConstantNode nullConstant = new ConstantNode(Type.NULL);
    static ConstantNode undefinedConstant = new ConstantNode(Type.UNDEFINED);
    static ConstantNode trueConstant = new ConstantNode(true);
    static ConstantNode falseConstant = new ConstantNode(false);

    Type type;

    public Type getType() {
        return type;
    }

    public boolean isNullValue() {
        return type == Type.NULL;
    }

    public boolean isUndefined() {
        return type == Type.UNDEFINED;
    }

    public boolean isBoolean() {
        return type == Type.BOOLEAN;
    }

    public boolean isString() {
        return type == Type.STRING;
    }

    public boolean isNumber() {
        return type == Type.FLOAT_NUMBER || type == Type.INT_NUMBER;
    }

    public boolean getBoolValue() {
        return boolValue;
    }

    public long getIntValue() {
        return intValue;
    }

    public double getFloatValue() {
        return floatValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    boolean boolValue;
    long intValue;
    double floatValue;
    String stringValue;


}

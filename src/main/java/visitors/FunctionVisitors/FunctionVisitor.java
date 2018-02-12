package visitors.FunctionVisitors;

import Types.Function;

public interface FunctionVisitor {

    boolean visit(Function function);

    default boolean preVisit() { return false;}
    default boolean postVisit() { return false; }
}

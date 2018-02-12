package visitors.CFGVisitors;

import Types.Function;
import Types.JSType;
import Types.TypeCollection;
import cfg.BasicBlock;

import java.util.List;

public interface Profiler {

    default boolean startProfiling() { return false; }
    default boolean finishProfiling() { return false; }

    default void profileFunction(Function function, List<JSType> profiledParams) {}
    default void profileReturn(Function function, JSType type) {}
    default void reportDeadBlock(BasicBlock block) {}
}

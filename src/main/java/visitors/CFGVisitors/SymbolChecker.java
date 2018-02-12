package visitors.CFGVisitors;

import Types.Function;
import Types.JSType;
import Types.TypeCollection;
import Utils.SourceLocation;
import cfg.References.Reference;

public interface SymbolChecker {
    default void checkSymbol(Reference reference, JSType type, TypeCollection impossibleTypes, SourceLocation location) {}
    default void setupFunction(Function function) {}
}

package Types;

import Types.Function;
import Utils.SourceLocation;
import visitors.FunctionVisitors.FunctionVisitor;
import jdk.nashorn.api.tree.LineMap;
import jdk.nashorn.api.tree.Tree;
import jdk.nashorn.api.tree.TreeVisitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class FunctionManager {

    private static List<Function> functions = new LinkedList<>();

    private static Function mainFunction;

    private static LineMap lineMap;
    private static String sourceName;

    private static final String mainName = "<main>";

    public static void initialize(LineMap map, String srcName) {
        lineMap = map;
        sourceName = srcName;
    }

    public static int getFunctionCount() {
        return functions.size();
    }

    public static Function getMainFunction() {
        return mainFunction;
    }

    public static LineMap getLineMap() {
        return lineMap;
    }
    public static String getSourceName() {
        return sourceName;
    }

    public static boolean hasFuncName(String idName) {
        return functions.stream().anyMatch(f -> f.getName().equals(idName));
    }

    public static Optional<Function> getFunction(String idName) {
        return functions.stream().filter(f -> f.getName().equals(idName)).findFirst();
    }

    public static Optional<Function> getFunction(Tree tree) {
        return functions.stream().filter(f -> f.getOriginalTree() == tree).findFirst();
    }


    public static Function registerFunction(String name, Tree tree, boolean isExpression) {
        Function function = new Function(name, tree, isExpression);

        long startLoc = tree.getStartPosition();
        long endLoc = tree.getEndPosition();

        function.setBeginLocation(makeSourceLocation(startLoc));
        function.setEndLocation(makeSourceLocation(endLoc));

        functions.add(function);

        return function;
    }

    public static Function registerMainFunction(Tree tree) {
        mainFunction = registerFunction(mainName, tree, false);
        return mainFunction;
    }


    public static SourceLocation makeSourceLocation(long location) {
        return new SourceLocation(getSourceName(), getLineMap().getLineNumber(location), getLineMap().getColumnNumber(location), location);
    }

    public static <R,P> void visitFunction(TreeVisitor<R, P> visitor, java.util.function.Function<Function, P> initializer) {
        for (Function f : functions) {
            f.getOriginalTree().accept(visitor, initializer.apply(f));
        }
    }

    public static void forEach(FunctionVisitor visitor) {

        boolean repeat;

        do {
            repeat = visitor.preVisit();;
            for (Function f : functions) {
                repeat |= visitor.visit(f);
            }
            repeat |= visitor.postVisit();
        } while (repeat);
    }

    public static void sortFunctions() {
        functions.sort((f1, f2) -> {
            // make sure <main> func is going first
            if (f1 == f2) return 0;
            if (f1.getName().equals("<main>")) return -1;
            if (f2.getName().equals("<main>")) return 1;

            return f1.getFuncID() - f2.getFuncID();

        });
    }
}

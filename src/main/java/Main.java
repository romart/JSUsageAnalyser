import Types.*;
import Utils.Parameters;
import Utils.UsageLocation;
import jdk.nashorn.api.scripting.NashornException;
import visitors.ASTVisitors.*;
import visitors.CFGVisitors.*;
import visitors.FunctionVisitors.ReachAnalyser;
import jdk.nashorn.api.tree.CompilationUnitTree;
import jdk.nashorn.api.tree.Parser;

import java.io.*;
import java.text.ParseException;
import java.util.Collection;
import java.util.TreeSet;


public class Main {


    public static void main(String[] args) throws ParseException {

        Parameters params = new Parameters(args);
        Collection<UsageLocation> exactLocations = new TreeSet<>();
        Collection<UsageLocation> possibleLocations = new TreeSet<>();

        runAnalyser(params.getInputFilePath(), params.getSymbolName(), exactLocations, possibleLocations);

        PrintStream stream = getOutputStream(params.getOutputFilePath());
        printResults(exactLocations, possibleLocations, stream);
    }

    public static void runAnalyser(String filePath, String symbol, Collection<UsageLocation> exactLocations, Collection<UsageLocation> possibleLocations) throws ParseException {
        CompilationUnitTree cut = buildAST(filePath);

        assert cut != null;
        buildFunctions(filePath, cut);

        buildCFG(cut);

        buildTypeHierarchy();

        profileTypes();

        Declaration declaration = Declaration.parse(symbol);
        SymbolChecker checker = new UsageSymbolChecker(exactLocations, possibleLocations, declaration);

        findUsages(checker);

        profileOpenWorld();
        findUsages(checker);
    }

    private static void profileOpenWorld() {
        FunctionManager.forEach(f -> {
            JSType[] params = f.getParamTypes();
            for (int i = 0; i < params.length; ++i) {
                params[i] = JSType.getObjectType();
            }
            return false;
        });
    }

    private static void findUsages(SymbolChecker checker) throws ParseException {

        TypeAnalyser analyser2 = new TypeAnalyser(checker);
        try {
            FunctionManager.forEach(analyser2);
        } catch (CFGException ex) {
            System.out.println("Unexpected exception: " + ex.getMessage());
        }
    }

    private static void printResults(Collection<UsageLocation> exactLocations, Collection<UsageLocation> possibleLocations, PrintStream stream) {
        stream.println("--- === EXACT USAGES === ---");
        for (UsageLocation u : exactLocations) {
            stream.println("    " + u.toString());
        }
        stream.println("--- === POSSIBLE USAGES === ---");
        for (UsageLocation u : possibleLocations) {
            stream.println("    " + u.toString());
        }
        stream.close();
    }

    private static PrintStream getOutputStream(String outFileName) {
        if (outFileName == null) return System.out;

        try {
            File outputFile = new File(outFileName);
            outputFile.createNewFile();
            return new PrintStream(outputFile);
        } catch (IOException e) {
            System.out.println("Could not create file " + outFileName);
            return System.out;
        }

    }

    private static void profileTypes() {
        Profiler profiler = new TypeProfiler();
        TypeAnalyser analyser = new TypeAnalyser(profiler);

        try {
            FunctionManager.forEach(analyser);
        } catch (IllegalCFGStateException iex) {
            System.out.println("Illegal state happened: ");
            if (iex.getSymbolName() != null) {
                System.out.println("  Symbol:     " + iex.getSymbolName());
            }
            System.out.println("  Reason:     " + iex.getMessage());
            System.out.println("  Location:   " + iex.getLocation());
            System.exit(-2);
        } catch (UnsupportedOperationFormatException uex) {
            System.out.println("Unsupported semantic operation has been met: " + uex.getTriggeredNode());
            System.out.println("  Reason:     " + uex.getMessage());
            System.out.println("  Location:   " + uex.getLocation());
            System.exit(-2);
        }

    }

    private static void buildTypeHierarchy() {
        TypeHierarchyBuilder typeHierarchyBuilder = new TypeHierarchyBuilder();
        typeHierarchyBuilder.process();
    }

    private static void buildFunctions(String filePath, CompilationUnitTree cut) {
        FunctionManager.initialize(cut.getLineMap(), filePath);
        FunctionCollector collector = new FunctionCollector();
        cut.accept(collector, null);
    }

    private static void buildCFG(CompilationUnitTree cut) {
        try {
            CFGBuilderVisitor builder = new CFGBuilderVisitor(false);
            FunctionManager.visitFunction(builder, BuildEnv::new);
            FunctionManager.registerMainFunction(cut);

            CFGBuilderVisitor mainBuild = new CFGBuilderVisitor(true);
            Function mainFunction = FunctionManager.getMainFunction();
            cut.accept(mainBuild, new BuildEnv(mainFunction));
        } catch (UnsupportedASTOperationException uex) {
            System.out.println("Unsupported operation has been met: " + uex.getTree().getKind());
            System.out.println("  Location:   " + uex.getLocation());
            System.exit(-2);
        } catch (SemanticASTException sex) {
            System.out.println("Semantic error at operation " + sex.getTree().getKind());
            System.out.println("  Reason:    " + sex.getMessage());
            System.out.println("  Location:  " + sex.getLocation());
            System.exit(-3);
        }

        FunctionManager.forEach(new ReachAnalyser());
        FunctionManager.sortFunctions();
    }

    private static CompilationUnitTree buildAST(String filePath) {
        File sourceFile = new File(filePath);
        Parser parser = Parser.create("--no-syntax-extensions");

        try {
            return parser.parse(sourceFile, null);
        } catch (NashornException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
            System.exit(-2);
        }
        return null;
    }
}
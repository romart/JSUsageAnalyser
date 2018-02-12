package Types;

import Types.JSType;
import Types.TypeCollection;
import Utils.SourceLocation;
import cfg.BasicBlock;
import cfg.Nodes.FunctionDeclarationNode;
import cfg.References.ThisReference;
import jdk.nashorn.api.tree.Tree;

import java.util.*;

public class Function {
    private static int idCounter;

    private String name;

    private SourceLocation beginLocation;

    private SourceLocation endLocation;

    private Collection<BasicBlock> blocks = new LinkedList<>();

    private BasicBlock entryBlock;

    private FunctionDeclarationNode declarationNode;

    private List<String> parameters = new LinkedList<>();

    private Set<String> localVars = new TreeSet<>();

    private Tree originalTree;

    private boolean isExpression;

    private ThisReference thisReference = new ThisReference();

    private JSType associatedType;

    private JSType holderType;

    private int funcID = idCounter++;

    private JSType paramTypes[];

    private JSType returnType = JSType.getUndefinedType();


    public Function(String name, Tree originaTree, boolean isExpression) {
        this.name = name;
        this.originalTree = originaTree;
        this.isExpression = isExpression;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public SourceLocation getBeginLocation() {
        return beginLocation;
    }
    public void setBeginLocation(SourceLocation location) {
        this.beginLocation = location;
    }

    public SourceLocation getEndLocation() {
        return endLocation;
    }
    public void setEndLocation(SourceLocation location) {
        this.endLocation = location;
    }

    public Collection<BasicBlock> getBlocks() {
        return blocks;
    }
    public void setBlocks(Collection<BasicBlock> blocks) {
        this.blocks = blocks;
    }

    public BasicBlock getEntryBlock() {
        return entryBlock;
    }
    public void setEntryBlock(BasicBlock entryBlock) {
        this.entryBlock = entryBlock;
    }

    public FunctionDeclarationNode getDeclarationNode() {
        return declarationNode;
    }
    public void setDeclarationNode(FunctionDeclarationNode declarationNode) {
        this.declarationNode = declarationNode;
    }

    public List<String> getParameters() {
        return parameters;
    }
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public Tree getOriginalTree() {
        return originalTree;
    }
    public void setOriginalTree(Tree originalTree) {
        this.originalTree = originalTree;
    }

    public boolean isExpression() { return isExpression;  }

    public void setExpression(boolean expression) {
        isExpression = expression;
    }
    public ThisReference getThisReference() {
        return thisReference;
    }

    public JSType getAssociatedType() {
        return associatedType;
    }
    public void setAssociatedType(JSType associatedType) {
        this.associatedType = associatedType;
    }

    public JSType getHolderType() {
        return holderType;
    }
    public void setHolderType(JSType type) {
        holderType = type;
    }

    public int getFuncID() {
        return funcID;
    }

    public JSType getReturnType() {
        return returnType;
    }
    public void setReturnType(JSType type) { returnType = type; }

    public JSType[] getParamTypes() {
        ensureParamTypes();
        return paramTypes;
    }


    public boolean hasVarName(String name) {
        return localVars.contains(name);
    }

    public void addParam(String p) {
        parameters.add(p);
        localVars.add(p);
    }

    public void addLocal(String l) {
        localVars.add(l);
    }

    public void addBlock(BasicBlock block) {
        if (entryBlock == null) {
            entryBlock = block;
        }

        blocks.add(block);
    }

    public int localsCount() {
        return localVars.size();
    }

    private void ensureParamTypes() {
        if (paramTypes == null) {
            paramTypes = new JSType[parameters.size()];
//            for (int i = 0; i < paramTypes.length; ++i) {
//                paramTypes[i] = JSType.getObjectType();
//            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());

        builder.append("@");
        builder.append(getFuncID());

        builder.append("(");
        boolean first = true;
        for (String p : getParameters()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(p);
        }

        builder.append(")");
        return builder.toString();
    }
}

package cfg;

import Types.Function;
import Utils.SourceLocation;
import cfg.Nodes.Node;

import java.util.Collection;
import java.util.LinkedList;

public class BasicBlock {

    private static int counter;

    private LinkedList<BasicBlock> successors = new LinkedList<>();

    private LinkedList<BasicBlock> predecessors = new LinkedList<>();
    private Function ownFunction;

    private Collection<Node> nodes = new LinkedList<>();

    private boolean isTerminated;

    private boolean isLoopHead;
    private int nestingLevel;

    private SourceLocation startLocation;
    private SourceLocation endLocation;

    private int blockID;


    public BasicBlock() {
        blockID = counter = getCounter() + 1;
    }

    public BasicBlock(Function function) {
        this();
        ownFunction = function;
        function.addBlock(this);
    }


    private static int getCounter() { return counter; }
    public static void resetCounter() { counter = 0; }

    public LinkedList<BasicBlock> getSuccessors() {
        return successors;
    }
    public LinkedList<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public Collection<Node> getNodes() {
        return nodes;
    }

    public int getNestingLevel() {
        return nestingLevel;
    }
    public void setNestingLevel(int nestingLevel) {
        this.nestingLevel = nestingLevel;
    }

    public void setTerminated() {
        isTerminated = true;
    }
    public boolean isTerminated() {
        return isTerminated;
    }

    public SourceLocation getStartLocation() {
        return startLocation;
    }
    public void setStartLocation(SourceLocation startLocation) {
        this.startLocation = startLocation;
    }

    public SourceLocation getEndLocation() {
        return endLocation;
    }
    public void setEndLocation(SourceLocation endLocation) {
        this.endLocation = endLocation;
    }

    public int getBlockID() {
        return blockID;
    }
    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    public void setLoopHead() {
        isLoopHead = true;
    }
    public boolean isLoopHead() {
        return isLoopHead;
    }

    public Function getOwnFunction() { return ownFunction; }

    public void addNode(Node node) {
        getNodes().add(node);
    }

    public void addSuccessor(BasicBlock block) {
        if (!getSuccessors().contains(block)) {
            getSuccessors().add(block);
            block.getPredecessors().add(this);
        }
    }

    public void removeSuccessor(BasicBlock block) {
        getSuccessors().remove(block);
        block.getPredecessors().remove(this);
    }

    public void addPredecessor(BasicBlock block) {
        if (!getPredecessors().contains(block)) {
            getPredecessors().add(block);
            block.getSuccessors().add(this);
        }
    }

    public void removePredecessor(BasicBlock block) {
        getPredecessors().remove(block);
        block.getSuccessors().remove(this);
    }

    public void setDead() { blockID = -1; }
    public boolean isAlive() { return blockID >= 0; }

    @Override
    public String toString() {
        return "BasicBlock #" + getBlockID();
    }
}

package visitors.ASTVisitors;

import cfg.BasicBlock;
import cfg.Nodes.CallNode;
import cfg.Nodes.Node;
import cfg.References.Reference;

public class BuildResult {

    private BasicBlock firstBlock;
    private BasicBlock lastBlock;

    private Node rawResult;
    private Reference resultReference;

    public BuildResult(Node outNode) {
        this(outNode, null);
    }

    public BuildResult(Node result, BasicBlock block) {
        this(result, block, block);
    }

    public BuildResult(Node result, Reference ref, BasicBlock block) {
        this(result, ref, block, block);
    }

    public BuildResult(Node result, BasicBlock firstBlock, BasicBlock lastBlock) {
        this(result, null, firstBlock, lastBlock);
    }

    public BuildResult(BasicBlock block) {
        this(block, block);
    }

    public BuildResult(Reference reference, BasicBlock appendBlock) {
        this(reference, appendBlock, appendBlock);
    }

    public BuildResult(BasicBlock firstBlock, BasicBlock lastBlock) {
        this(null, null, firstBlock, lastBlock);
    }

    public BuildResult(Reference property, BasicBlock firstBlock, BasicBlock lastBlock) {
        this(null, property, firstBlock, lastBlock);
    }

    public BuildResult(Node result, Reference ref, BasicBlock firstBlock, BasicBlock lastBlock) {
        setRawResult(result);
        setResultReference(ref);
        setBounds(firstBlock, lastBlock);
    }

    public BuildResult(Reference ref) {
        this.setResultReference(ref);
    }

    private void setBounds(BasicBlock firstBlock, BasicBlock lastBlock) {
        setFirstBlock(firstBlock);
        setLastBlock(lastBlock);
    }
    public BasicBlock getFirstBlock() {
        return firstBlock;
    }
    public void setFirstBlock(BasicBlock block) {
        firstBlock = block;
    }

    public BasicBlock getLastBlock() {
        return lastBlock;
    }
    public void setLastBlock(BasicBlock block) {
        lastBlock = block;
    }

    public Node getResult() {
        if (getRawResult() instanceof CallNode) {
            return getRawResult();
        }
        if (getResultReference() != null) {
            return getResultReference().asReadNode();
        }
        return getRawResult();
    }

    public void setRawResult(Node rawResult) {
        this.rawResult = rawResult;
    }
    public Node getRawResult() { return rawResult; }

    public Reference getResultReference() {
        return resultReference;
    }
    public void setResultReference(Reference resultReference) {
        this.resultReference = resultReference;
    }

    public static BuildResult makeWithBlock(BasicBlock block) {
        return new BuildResult(block, block);
    }

}

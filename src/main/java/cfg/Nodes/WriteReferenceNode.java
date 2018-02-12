package cfg.Nodes;

import cfg.References.Reference;

public abstract class WriteReferenceNode extends AccessReferenceNode {
    Node sourceNode;

    protected WriteReferenceNode(Reference reference, Node sourceNode) {
        super(reference);
        this.sourceNode = sourceNode;
    }

    public Node getSourceNode() {
        return sourceNode;
    }


}

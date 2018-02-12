package visitors.CFGVisitors;

import Utils.SourceLocation;
import cfg.Nodes.Node;

public abstract class CFGException extends Error {
    Node triggeredNode;

    protected CFGException(Node node, String message) {
        super(message);
        this.triggeredNode = node;
    }

    public SourceLocation getLocation() {
        return triggeredNode.getLocation();
    }

    public Node getTriggeredNode() {
        return triggeredNode;
    }
}

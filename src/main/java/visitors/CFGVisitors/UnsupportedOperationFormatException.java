package visitors.CFGVisitors;

import cfg.Nodes.Node;

public class UnsupportedOperationFormatException extends CFGException {
    public UnsupportedOperationFormatException(Node node, String message) {
        super(node, message);
    }
}

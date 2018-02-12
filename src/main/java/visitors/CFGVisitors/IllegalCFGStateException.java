package visitors.CFGVisitors;

import cfg.Nodes.Node;

public class IllegalCFGStateException extends CFGException {

    private String symbolName;

    public IllegalCFGStateException(String symbolName, String message) {
        super(null, message);
    }

    public String getSymbolName() {
        return symbolName;
    }
}

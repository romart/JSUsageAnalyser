package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Property;

public class ReadPropertyNode extends ReadReferenceNode {

    public ReadPropertyNode(Property property) {
        super(property);
    }

    public Property getProperty() {
        return (Property) getReference();
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitReadPropertyNode(this, p);
    }
}

package cfg.Nodes;

import visitors.CFGVisitors.NodeVisitor;
import cfg.References.Property;

public class WritePropertyNode extends WriteReferenceNode {

    public WritePropertyNode(Property property, Node sourceNode) {
        super(property, sourceNode);
    }

    public Property getProperty() {
        return (Property) getReference();
    }

    @Override
    public <R, P> R visit(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitWritePropertyNode(this, p);
    }
}

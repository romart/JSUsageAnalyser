package visitors.CFGVisitors;

import Types.*;
import Utils.SourceLocation;
import cfg.*;
import cfg.Nodes.*;
import cfg.References.FuncReference;
import cfg.References.Property;
import cfg.References.Reference;
import visitors.FunctionVisitors.FunctionVisitor;

import java.util.*;

import static cfg.References.Reference.Type.VAR;

public class TypeAnalyser implements FunctionVisitor, NodeVisitor<TypeAnalyser.VisitResult, TypeAnalyser.VisitEnv> {

    @Override
    public VisitResult visitBinary(BinaryOpNode node, VisitEnv visitEnv) {

        Node left = node.getLeft();
        Node right = node.getRight();

        VisitResult leftResult = left.visit(this, visitEnv);
        VisitResult rightResult = right.visit(this, visitEnv);

        Reference leftReference = leftResult.getReference();
        Reference rightReference = rightResult.getReference();

        VisitResult result = null;

        if (leftReference != null && rightReference != null) {
            if (leftReference.getType() != VAR || rightReference.getType() != VAR) {
                throw new UnsupportedOperationFormatException(node, "Compare operation should contain either two vars or var and null-const");
            }

            JSType resultType;
            JSType leftType = leftResult.getType();
            JSType rightType = rightResult.getType();

            // the first problem is what type should we peek?
            // Let choose type from var we know much about
            if (leftType == null || leftType == JSType.getUndefinedType()) {
                resultType = rightType;
            } else if (rightType == null || rightType == JSType.getUndefinedType()) {
                resultType = leftType;
            } else {
                // if we know something about both of them we can compute shared type and take it
                if (leftType.isSubtypeOf(rightType)) {
                    resultType = leftType;
                } else if (rightType.isSubtypeOf(leftType)) {
                    resultType = rightType;
                } else {
                    // if left and right types are from the different branches of type-tree that means there is no shared type
                    // in other words such check is always failing
                    resultType = null;
                }
            }
            TypeCollection impossibleTypes = leftResult.getImpossibleTypes().intersect(rightResult.getImpossibleTypes());

            result = new VisitResult(leftReference, rightReference, resultType, impossibleTypes);
        } else {
            Reference reference;
            ConstantNode constantNode;
            if (left instanceof ConstantNode) {
                constantNode = (ConstantNode) left;
                reference = rightReference;
            } else if (right instanceof ConstantNode) {
                reference = leftReference;
                constantNode = (ConstantNode) right;
            } else {
                throw new UnsupportedOperationFormatException(node, "Only null-checks are supported");
            }

            if (reference.getType() != VAR || !constantNode.isNullValue()) {
                throw new UnsupportedOperationFormatException(node, "Null-check should be with variable");
            }

            result = new VisitResult(reference, VisitResult.CondType.NULLCHECK);
        }

        if (node.getOp() == BinaryOpNode.Operation.NE) {
            result.invert();
        }

        return result;
    }

    @Override
    public VisitResult visitCall(CallNode node, VisitEnv visitEnv) {
        VisitResult readRefRes;
        if (node.getTargetReference().getType() == Reference.Type.PROPERTY) {
            Reference targetReference = ((Property) node.getTargetReference()).getBase();
            readRefRes = extractTypes(targetReference, node.getTarget().getLocation(), visitEnv);
        } else {
            readRefRes = readReference(node.getTarget(), visitEnv);
        }
        checker.checkSymbol(node.getTargetReference(), readRefRes.getType(), readRefRes.getImpossibleTypes(), node.getLocation());
        Collection<Function> possibleTargets = getPossibleTargets(readRefRes, node.getTargetReference().getName());
        JSType retType = composeReturnType(possibleTargets);

        processFunctionsParams(possibleTargets, node.getParams(), visitEnv);

        if (node.getTarget().getReference().getType() == Reference.Type.EXTERNAL) {
            // because it might be everything
            retType = JSType.getObjectType();
        }

        return new VisitResult(retType);
    }

    @Override
    public VisitResult visitConstant(ConstantNode node, VisitEnv visitEnv) {

        if (node.isNumber() || node.isBoolean()) {
            return new VisitResult(JSType.getPrimitiveType());
        }
        if (node.isString()) {
            return new VisitResult(JSType.getStringType());
        }
        if (node.isNullValue()) {
            return new VisitResult(JSType.getNullType());
        }
        if (node.isUndefined()) {
            return new VisitResult(JSType.getUndefinedType());
        }

        return new VisitResult();
    }

    @Override
    public VisitResult visitFunctionDeclaration(FunctionDeclarationNode node, VisitEnv visitEnv) {
        return new VisitResult();
    }

    @Override
    public VisitResult visitIf(IfNode node, VisitEnv visitEnv) {
        VisitResult condResult = node.getCondition().visit(this, visitEnv);

        Reference reference = condResult.getReference();

        if (reference == null || reference.getType() != VAR) {
            throw new UnsupportedOperationFormatException(node, "Required non-null var reference in IfNode");
        }

        String varName = reference.getName();

        BasicBlock thenBB = node.getTrueBlock();
        BasicBlock elseBB = node.getFalseBlock();
        BasicBlock contBB = node.getContinueBlock();

        Block thenBlock = toBlock(thenBB);
        Block elseBlock = toBlock(elseBB);
        Block contBlock = contBB.isAlive() ? toBlock(contBB) : null;

        thenBlock.initState(visitEnv.getCurrentState());
        elseBlock.initState(visitEnv.getCurrentState());
        if (contBlock != null) {
            contBlock.initState(visitEnv.getCurrentState());
        }

        LocalState thenState = thenBlock.getState().getVarState(varName);
        LocalState elseState = elseBlock.getState().getVarState(varName);

        switch (condResult.getCondType()){
            case INSTANCEOF: {
                // set up LCA type here
                JSType lcaType = condResult.getLCAType();
                JSType existingType = condResult.getType();
                JSType newThenType = lcaType;
                JSType newElseType = existingType;
                TypeCollection newThenImpossibleTypes = condResult.getImpossibleTypes();
                TypeCollection newElseImpossibleTypes = new TypeCollection();

                // first, figure out what types we have
                if (existingType == JSType.getUndefinedType() || existingType == null) {
                    // to make sure we have this case checked first
                    newThenType = lcaType;
                    newElseType = existingType;
                } else if (existingType.isSubtypeOf(lcaType)) {
                    // if lca type is more general than known type
                    newThenType = existingType;
                    newElseType = JSType.getUndefinedType();
                } else if (!lcaType.isSubtypeOf(existingType)) {
                    // if types are from the different sub-trees
                    //    A
                    //  /  \
                    // B   C
                    // where `existing` is B and `lca` is C
                    // In that case there is no possible type under true-branch
                    // let mark it as null
                    newThenType = null;
                    newElseType = existingType;
                }

                // second, what kind of IF is it?
                if (node.isDiamond()) {

                    // add lca type to the original-else branch
                    newElseImpossibleTypes.addType(lcaType);

                    JSType newTrueType = newThenType;
                    JSType newFalseType = newElseType;
                    TypeCollection trueImpTypes = newThenImpossibleTypes;
                    TypeCollection falseImpTypes = newElseImpossibleTypes;

                    // if condition is inverted we need to flip our types
                    if (condResult.isInverted()) {
                        newTrueType = newElseType;
                        newFalseType = newThenType;
                        trueImpTypes = newElseImpossibleTypes;
                        falseImpTypes = newThenImpossibleTypes;
                    }


                    // finally set types up

                    // so here we have to split type-tree into two different tree:
                    // first contains types BELOW lca type including it, and
                    // second contains types ABOVE lca excluding it
                    thenBlock.getState().setLocalType(varName, newTrueType, trueImpTypes);
                    elseBlock.getState().setLocalType(varName, newFalseType, falseImpTypes);

                    // mark both as merged
                    elseState.setMerged();
                    thenState.setMerged();
                } else {
                    // if this case
                    // if () {
                    // }
                    // there is no else branch so we should only care about then branch

                    if (condResult.isInverted()) {
                        // that means that instance-check has failed and we keep old type and add lca type as impossible
                        newThenImpossibleTypes.addType(lcaType);
                        thenBlock.getState().setLocalType(varName, existingType, newThenImpossibleTypes);
                    } else {
                        // otherwise set up new type for then-branch
                        thenBlock.getState().setLocalType(varName, newThenType, newThenImpossibleTypes);
                    }
                    thenState.setMerged();
                }

                break;
            }
            case VARCHECK: {
                JSType lcaType = condResult.getType();
                TypeCollection impossibleTypes = condResult.getImpossibleTypes();
                Reference scndRef = condResult.getSndReference();

                if (scndRef == null || scndRef.getType() != VAR) {
                    throw new UnsupportedOperationFormatException(node, "Required non-null var reference in IfNode");
                }

                // that means we have two vars with the same set of [im]possible types, so assign them
                String frstVarName = varName;
                String scndVarName = scndRef.getName();

                LocalState currentScndVarState = visitEnv.getCurrentState().getVarState(scndVarName);

                if (condResult.isInverted() && !node.isDiamond()) {
                    // in this case we know nothing so skip it
                    break;
                }

                Block block = condResult.isInverted() ? elseBlock : thenBlock;

                LocalState frstVarState = block.getState().getVarState(frstVarName);
                LocalState scndVarState = block.getState().getVarState(scndVarName);

                block.getState().setLocalType(frstVarName, lcaType, impossibleTypes);
                block.getState().setLocalType(scndVarName, lcaType, impossibleTypes);

                frstVarState.setMerged();
                scndVarState.setMerged();
                break;
            }
            case NULLCHECK: {

                // it is a null check so there is no possible type for that var
                if (node.isDiamond()) {
                    if (condResult.isInverted()) {
                        elseState.setType(JSType.getNullType());
                        elseState.setMerged();
                    } else {
                        thenState.setType(JSType.getNullType());
                        thenState.setMerged();
                    }
                } else {
                    if (!condResult.isInverted()) {
                        thenState.setType(JSType.getNullType());
                        thenState.setMerged();
                    }
                }

                break;
            }

            default: throw new UnsupportedOperationFormatException(node, "Other condition constructions are not implemented yet");

        }


        return new VisitResult();
    }

    @Override
    public VisitResult visitInstanceOf(InstanceOfNode node, VisitEnv visitEnv) {

        VisitResult checkResult = node.getCheckReference().visit(this, visitEnv);
        JSType lcaType = TypeManager.getType(node.getTypeRefernce().getName());
        JSType currentType = checkResult.getType();

        return new VisitResult(currentType, lcaType, checkResult.getImpossibleTypes(), checkResult.getReference());
    }

    @Override
    public VisitResult visitJumpNode(JumpNode node, VisitEnv visitEnv) {
        return new VisitResult();
    }

    @Override
    public VisitResult visitNewObjNode(NewObjectNode node, VisitEnv visitEnv) {
        JSType type = TypeManager.getType(node.getClassReference().getName());
        if (type == null) {
            return new VisitResult(JSType.getObjectType());
        }
        return new VisitResult(type);
    }

    @Override
    public VisitResult visitReadPropertyNode(ReadPropertyNode node, VisitEnv visitEnv) {
        VisitResult result = extractTypes(node.getProperty().getBase(), node.getLocation(), visitEnv);
        checker.checkSymbol(node.getReference(), result.getType(), result.getImpossibleTypes(), node.getLocation());
        return result;
    }

    @Override
    public VisitResult visitReadThisReference(ReadThisReferenceNode node, VisitEnv visitEnv) {

        return new VisitResult(getCurrentFunction().getHolderType());
    }

    @Override
    public VisitResult visitReadFuncReference(ReadFunctionReferenceNode node, VisitEnv visitEnv){
        FuncReference funcReference = node.getFuncReference();
        String functionName = funcReference.getName();

        Optional<Function> func = FunctionManager.getFunction(functionName);


        if (func.isPresent()) {
            checker.checkSymbol(funcReference, getCurrentFunction().getHolderType(), TypeCollection.getEmpty(), node.getLocation());
            return new VisitResult(func.get());
        } else {
            return new VisitResult(funcReference);
        }
    }

    @Override
    public VisitResult visitReadVarNode(ReadVarNode node, VisitEnv visitEnv) {

        State state = visitEnv.getCurrentState();
        String varName = node.getVariableRef().getName();
        VisitResult result;

        if (state.hasLocalVar(varName)) {
             result = state.fillVisitResult(varName);
        } else {
            // external variable, might be any type
            result = new VisitResult(JSType.getObjectType());
        }

        result.setReference(node.getReference());

        return result;
    }

    //
    @Override
    public VisitResult visitReadExReferenceNode(ReadExReferenceNode node, VisitEnv env) {

        checker.checkSymbol(node.getExternalReference(), getCurrentFunction().getHolderType(), TypeCollection.getEmpty(), node.getLocation());

        return new VisitResult(JSType.getObjectType());
    }

    @Override
    public VisitResult visitWriteExReferenceNode(WriteExReferenceNode node, VisitEnv env) {
        // visit to cover possible usage of symbol in the `source` node
        VisitResult source = node.getSourceNode().visit(this, env);

        return new VisitResult();
    }
    @Override
    public VisitResult visitReturnNode(ReturnNode node, VisitEnv visitEnv) {

        if (node.getReturnNode() != null) {
            VisitResult result = node.getReturnNode().visit(this, visitEnv);
            profiler.profileReturn(getCurrentFunction(), result.getType());
            return result;
        }
        return new VisitResult();
    }

    @Override
    public VisitResult visitUnaryNode(UnaryOpNode node, VisitEnv visitEnv) {

        VisitResult exprRes = node.getExpr().visit(this, visitEnv);

        if (node.getOp() == UnaryOpNode.Operation.LNEG) {
            // check for pattern !(cond)
            exprRes.invert();
        }

        return exprRes;
    }

    @Override
    public VisitResult visitVarDeclarationNode(VarDeclarationNode node, VisitEnv visitEnv) {
        visitEnv.getCurrentState().addVariable(node.getVarName());
        return new VisitResult();
    }

    @Override
    public VisitResult visitWritePropertyNode(WritePropertyNode node, VisitEnv visitEnv) {
        // we do not track properties types so just visit left and right sides
        VisitResult def = extractTypes(node.getReference(), node.getLocation(), visitEnv);
        VisitResult src = node.getSourceNode().visit(this, visitEnv);

        return new VisitResult();
    }

    @Override
    public VisitResult visitWriteVarNode(WriteVarNode node, VisitEnv visitEnv) {
        String varName = node.getVariable().getName();
        VisitResult sourceRes = node.getSourceNode().visit(this, visitEnv);
        visitEnv.getCurrentState().setLocalType(varName, sourceRes.getType(), sourceRes.getImpossibleTypes());

        return new VisitResult();
    }

    private JSType composeReturnType(Collection<Function> possibleTargets) {
        JSType returnType = null;
        for (Function func: possibleTargets) {
            if (returnType == null) {
                returnType = func.getReturnType();
            }
            returnType = JSType.mergeTypes(returnType, func.getReturnType());
        }

        return returnType == null ? JSType.getUndefinedType() : returnType;
    }

    private Collection<Function> getPossibleTargets(VisitResult readRefRes, String methodName) {

        Collection<Function> possibleFunctions = new LinkedList<>();

        if (readRefRes.getFunction() != null) {
            possibleFunctions.add(readRefRes.getFunction());
        } else {
            collectMethodsAbove(methodName, readRefRes.getType(), possibleFunctions);
        }

        return possibleFunctions;
    }

    private void collectMethodsAbove(String methodName, JSType type, Collection<Function> possibleFunctions) {


       if (type == null) return;
        if (type.hasProperty(methodName)) {
            JSType methodType = type.propertyType(methodName);
            if (methodType.isMethodProperty()) {
                possibleFunctions.add(methodType.getParentFunction());
            }
        }

        for (JSType cType : type.getChildTypes()) {
            collectMethodsAbove(methodName, cType, possibleFunctions);
        }
    }

    private void processFunctionsParams(Collection<Function> targetFunction, List<Node> params, VisitEnv env) {

        List<JSType> processedParams = processParams(params, env);

        for (Function function : targetFunction) {
            profiler.profileFunction(function, processedParams);
        }
    }

    private List<JSType> processParams(List<Node> params, VisitEnv env) {

        ArrayList<JSType> processedParams = new ArrayList<>(params.size());

        for (Node p : params) {
            VisitResult res = p.visit(this, env);
            processedParams.add(res.getType());
        }

        return processedParams;
    }

    private VisitResult readReference(ReadReferenceNode target, VisitEnv visitEnv) {

        return target.visit(this, visitEnv);
    }

    private VisitResult extractTypes(Reference reference, SourceLocation location, VisitEnv env) {
        switch (reference.getType()) {
            case VAR: return env.getCurrentState().fillVisitResult(reference.getName());
            case PROPERTY: {
                Property property = (Property) reference;
                VisitResult res = extractTypes(property.getBase(), location, env);
                checker.checkSymbol(property, res.getType(), res.getImpossibleTypes(), location);

                JSType propType = res.getType().propertyType(property.getName());

                return new VisitResult(propType);
            }

            case EXTERNAL: {
                checker.checkSymbol(reference, getCurrentFunction().getHolderType(), TypeCollection.getEmpty(), location);
                return new VisitResult(JSType.getObjectType());
            }
            case FUNCTION: {
                Optional<Function> optFunction = FunctionManager.getFunction(reference.getName());
                if (optFunction.isPresent()) {
                    Function function = optFunction.get();
                    checker.checkSymbol(reference, function.getHolderType(), TypeCollection.getEmpty(), location);
                    return new VisitResult(function.getAssociatedType());
                } else {
                    return new VisitResult();
                }
            }
            case THIS: {
                checker.checkSymbol(reference, getCurrentFunction().getHolderType(), TypeCollection.getEmpty(), location);
                return new VisitResult(getCurrentFunction().getHolderType());
            }
        }

        return new VisitResult();
    }

    static class VisitResult {
        enum CondType {
            NOCOND,
            INSTANCEOF,
            NULLCHECK,
            VARCHECK
        }

        JSType type;

        TypeCollection impossibleTypes = new TypeCollection();

        Function function;
        Reference reference;
        Reference reference2;

        JSType lcaType;
        boolean inverted;

        CondType condType = CondType.NOCOND;

        Optional<Boolean> staticCondition = Optional.empty();


        VisitResult(JSType type) {
            this.type = type;
        }

        VisitResult() {}

        VisitResult(JSType type, JSType lcaType, TypeCollection impossibleTypes, Reference reference) {
            this.type = type;
            this.lcaType = lcaType;
            this.impossibleTypes = impossibleTypes;
            this.reference = reference;
            this.condType = CondType.INSTANCEOF;
        }

        VisitResult(Function function) {
            setFunction(function);
        }

        VisitResult(Reference reference) {
            this.reference = reference;
        }

        VisitResult(Reference reference, CondType nullcheck) {
            this.reference = reference;
            assert nullcheck == CondType.NULLCHECK;
            this.condType = nullcheck;
        }

        VisitResult(boolean staticCondition) {
            this.staticCondition = Optional.of(staticCondition);
        }

        VisitResult(Reference first, Reference second, JSType type, TypeCollection impossibleTypes) {
            this.reference = first;
            this.reference2 = second;
            this.type = type;
            this.impossibleTypes = impossibleTypes;
            this.condType = CondType.VARCHECK;
        }

        TypeCollection getImpossibleTypes() {
            return impossibleTypes;
        }

        Function getFunction() { return function; }
        void setFunction(Function function) { this.function = function; }

        Reference getReference() {
            return reference;
        }
        void setReference(Reference reference) {
            this.reference = reference;
        }
        Reference getSndReference() { return reference2; }

        JSType getLCAType() {
            return lcaType;
        }
        JSType getType() { return type; }

        boolean isInverted() {
            return inverted;
        }
        void invert() {
            this.inverted = !this.inverted;
        }

        CondType getCondType() { return condType; }

        void addImpossibleType(JSType type) {
            impossibleTypes.addType(type);
        }
        void addImpossibleTypes(TypeCollection types) {
            impossibleTypes.addTypes(types);
        }

        boolean hasStaticCondition() { return staticCondition.isPresent(); }
        boolean getStaticCondition() { return staticCondition.get(); }
        void setStaticCondition(boolean v) { staticCondition = Optional.of(v); }
    }

    static class VisitEnv {

        State currentState;

        State getCurrentState() {
            return currentState;
        }

        VisitEnv(State state) {
            this.currentState = state;
        }
    }

    public TypeAnalyser(SymbolChecker checker) {
        this.functions = new FunctionContext[FunctionManager.getFunctionCount()];
        this.checker = checker;

    }

    public TypeAnalyser(Profiler profiler) {
        this.functions = new FunctionContext[FunctionManager.getFunctionCount()];
        this.profiler = profiler;
    }

    private class LocalState {
        final String localName;

        JSType type;
        TypeCollection impossibleTypes = new TypeCollection();

        Map<String, JSType> fieldMap;

        // used to avoid double-merge

        boolean merged = false;

        LocalState(String localName) {
            this.localName = localName;
            this.type = JSType.getUndefinedType();
        }

        LocalState(String localName, JSType type) {
            this.localName = localName;
            this.type = type;
        }

        LocalState(LocalState copy) {
            this.localName = copy.getLocalName();
            this.type = copy.type;
            this.impossibleTypes.addTypes(copy.impossibleTypes);
        }

        public TypeCollection getImpossibleTypes() {
            return impossibleTypes;
        }

        JSType getType() {
            return type;
        }
        void setType(JSType type) {
            this.type = type;
        }

        String getLocalName() {
            return localName;
        }

        boolean isMerged() { return merged; }
        void setMerged() { merged = true; }
        void resetMerged() { merged = false; }
    }

    private class State {

        private Map<String, LocalState> localTypes = new HashMap<>();

        private void profileInitialParams(Function func) {
            JSType[] possibleParamsTypes = func.getParamTypes();

            int i = 0;
            for (String pName : func.getParameters()) {
                JSType profile = possibleParamsTypes[i++];
                if (profile == null) {
                    profile = JSType.getUndefinedType();
                }
                LocalState pState = new LocalState(pName, profile);
                localTypes.put(pName, pState);
            }
        }

        boolean meet(State other) {
            Map<String, LocalState> thisTypes = localTypes;
            Map<String, LocalState> otherTypes = other.localTypes;


            final boolean[] changed = new boolean[1];

            LinkedList<LocalState> diff = new LinkedList<>();
            otherTypes.forEach((k, v) -> {

                if (thisTypes.containsKey(k)) {
                    LocalState thisState = thisTypes.get(k);
                    if (thisState.isMerged()) {
                        thisState.resetMerged();
                        return;
                    }

                    // 1. meet type
                    JSType newType = mergeTypes(thisState.getType(), v.getType());
                    changed[0] |= thisState.getType() != newType;
                    thisState.setType(newType);

                    // 2. meet impossible types
                    changed[0] |= thisState.getImpossibleTypes().intersectWith(v.getImpossibleTypes());
                } else {
                    changed[0] = true;
                    diff.add(v);
                }
            });

            for (LocalState state : diff) {
                thisTypes.put(state.getLocalName(), new LocalState(state));
            }

            return changed[0];
        }

        private JSType mergeTypes(JSType thisType, JSType otherType) {

            if (thisType == null) {
                return otherType;
            }

            if (otherType == null) {
                return thisType;
            }

            return JSType.mergeTypes(thisType, otherType);
        }

        void copyInto(State other) {
            Map<String, LocalState> copyMap = new HashMap<>();

            localTypes.forEach((k, v) -> {
                LocalState copyState = new LocalState(k);
                copyState.setType(v.getType());
                copyState.getImpossibleTypes().unionWith(v.getImpossibleTypes());
                copyMap.put(k, copyState);
            });

            other.localTypes = copyMap;
        }

        void setLocalType(String localName, JSType type, TypeCollection impossibleTypes) {
            LocalState localState = localTypes.get(localName);
            localState.setType(type);
            localState.getImpossibleTypes().addTypes(impossibleTypes);
        }

        boolean hasLocalVar(String localName) {
            return localTypes.containsKey(localName);
        }

        VisitResult fillVisitResult(String varName) {
            LocalState state = localTypes.get(varName);
            VisitResult result = new VisitResult(state.getType());
            result.addImpossibleTypes(state.getImpossibleTypes());
            return result;
        }

        void addVariable(String varName) {
            if (localTypes.containsKey(varName)) {
                throw new IllegalCFGStateException(varName, "Redeclaration of variable");
            }

            localTypes.put(varName, new LocalState(varName));
        }

        LocalState getVarState(String varName) {
            return localTypes.get(varName);
        }


        boolean isEmpty() {
            return localTypes.isEmpty();
        }
    }

    private class Block {

        BasicBlock basicBlock;

        State state;

        int preOrder = -1;
        int postOrder = -1;

        Collection<Block> successors;
        Collection<Block> predecessors;

        Block nextWorkList;
        boolean isOnWorkList;


        Block(BasicBlock basicBlock) {
            this.basicBlock = basicBlock;
        }

        State getState() {
            return state;
        }

        int getPreOrder() {
            return preOrder;
        }
        void setPreOrder(int preOrder) {
            this.preOrder = preOrder;
        }

        int getPostOrder() {
            return postOrder;
        }
        void setPostOrder(int postOrder) {
            this.postOrder = postOrder;
        }

        boolean isVisited() {
            return preOrder >= 0;
        }
        boolean isPostVisited() {
            return postOrder >= 0;
        }

        Collection<Block> getSuccessors() {
            if (successors == null) {
                initSuccessors();
            }
            return successors;

        }
        Collection<Block> getPredecessors() {
            if (predecessors == null) {
                initPredecessors();
            }
            return predecessors;
        }

        Block getNextWorkList() {
            return nextWorkList;
        }
        void setNextWorkList(Block nextWorkList) {
            this.nextWorkList = nextWorkList;
        }

        boolean isOnWorkList() {
            return isOnWorkList;
        }
        void setOnWorkList(boolean onWorkList) {
            isOnWorkList = onWorkList;
        }

        private void initSuccessors() {
            successors = new LinkedList<>();

            for (BasicBlock succ : basicBlock.getSuccessors()) {
                successors.add(toBlock(succ));
            }
        }

        private void initPredecessors() {
            predecessors = new LinkedList<>();
            for (BasicBlock pred : basicBlock.getPredecessors()) {
                predecessors.add(toBlock(pred));
            }
        }


        boolean meetState(State other) {
            return state.meet(other);
        }

        Collection<Node> getNodes() {
            return basicBlock.getNodes();
        }

        boolean isLoopHead() {
            return basicBlock.isLoopHead();
        }

        void reset() {
            preOrder = postOrder = -1;
            successors = predecessors = null;
            nextWorkList = null;
            state = new State();
        }

        void initState(State initState) {
            if (getState().isEmpty()) {
                initState.copyInto(getState());
            }
        }

    }

    private class FunctionContext {
        Function function;
        Block[] blocks;


        FunctionContext(Function function) {
            this.function = function;

            int idCounter = 0;
            blocks = new Block[function.getBlocks().size()];
            for (BasicBlock block : function.getBlocks()) {
                block.setBlockID(idCounter);
                blocks[idCounter] = new Block(block);
                idCounter++;
            }
        }

        Function getFunction() {
            return function;
        }

        Block[] getBlocks() {
            return blocks;
        }

        void reset() {
            for (Block block : blocks) {
                block.reset();
            }
        }

    }

    private FunctionContext getCurrentContext() {
        return currentContext;
    }
    private void setCurrentContext(FunctionContext context) {
        currentContext = context;
    }

    private FunctionContext[] functions;
    private FunctionContext currentContext;

    private Block workList;

    private Profiler profiler = new Profiler() {};
    private SymbolChecker checker = new SymbolChecker() {};

    private Block getWorkList() {
        return workList;
    }
    private void setWorkList(Block workList) {
        this.workList = workList;
    }
    private boolean isWorkingListEmpty() {
        return workList == null;
    }


    private void processBlocks(Function function) {

        int nextPre;
        int nextPo = nextPre = 0;

        setCurrentContext(getContextForFunction(function));
        getCurrentContext().reset();

        State state = new State();

        Block entryBlock = toBlock(getCurrentFunction().getEntryBlock());
        // get profile information
        entryBlock.getState().profileInitialParams(getCurrentFunction());

        Stack<Block> stack = new Stack<>();
        stack.push(entryBlock);


        while (!stack.empty()) {

            int size = stack.size();
            Block block = stack.peek();

            if (!block.isVisited()) {
                block.setPreOrder(nextPre++);

                processBlock(block, state);
            } else if (!block.isPostVisited()) {

                for (Block succ : block.getSuccessors()) {
                    if (!succ.isVisited()) {
                        stack.push(succ);
                    }
                }

                // check whether all block's successors have been visited
                if (stack.size() == size) {
                    // if so pop current block and setup its PostOrder index
                    stack.pop();
                    block.setPostOrder(nextPo++);

                    // does it need additional processing?
                    // the loop head defiantly does
                    if (block.isLoopHead() && !block.isOnWorkList()) {
                        addToWorkList(block);
                    }
                }
            } else {
                stack.pop();
            }
        }

        while (!isWorkingListEmpty()) {
            Block block = nextWorkItem();

            processBlock(block, state);
        }
    }

    private void processBlock(Block block, State state) {

        State blockState = block.getState();
        blockState.copyInto(state);

        for (Node node : block.getNodes()) {
            node.visit(this, new VisitEnv(state));
        }

        for (Block succ : block.getSuccessors()) {
            if (succ.meetState(state)) {
                if (succ.isPostVisited() && !succ.isOnWorkList()) {
                    addToWorkList(succ);
                }
            }
        }

    }

    private Block nextWorkItem() {

        Block block = getWorkList();
        setWorkList(block.getNextWorkList());

        block.setNextWorkList(null);
        block.setOnWorkList(false);

        return block;
    }

    private void addToWorkList(Block block) {
        block.setOnWorkList(true);

        Block prev = null;
        Block current = getWorkList();

        while (current != null) {
            if (!current.isPostVisited() || block.getPostOrder() > current.getPostOrder()) {
                break;
            }
            prev = current;
            current = current.getNextWorkList();
        }

        if (prev == null) {
            block.setNextWorkList(getWorkList());
            setWorkList(block);
        } else {
            block.setNextWorkList(current);
            prev.setNextWorkList(block);
        }
    }


    @Override
    public boolean visit(Function function) {

        checker.setupFunction(function);
        processBlocks(function);
        return false;
    }

    @Override
    public boolean preVisit() {
        return profiler.startProfiling();
    }

    @Override
    public boolean postVisit() {
        return profiler.finishProfiling();
    }

    private void ensureFunctionContext(Function function) {
        FunctionContext context = functions[function.getFuncID()];
        if (context == null) {
            context = new FunctionContext(function);
            functions[function.getFuncID()] = context;
        }
    }

    private FunctionContext getContextForFunction(Function function) {
        ensureFunctionContext(function);
        return functions[function.getFuncID()];
    }

    private Block toBlock(BasicBlock bb) {
        return getCurrentContext().getBlocks()[bb.getBlockID()];
    }

    private Function getCurrentFunction() {
        return getCurrentContext().getFunction();
    }
}

package visitors.ASTVisitors;

import Types.Function;
import cfg.BasicBlock;

public class BuildEnv {

    private BasicBlock appendBlock;
    private BuildEnv parent;
    private Function function;

    public BuildEnv(Function function) {
        this.setFunction(function);
    }

    public BuildEnv(BuildEnv parent) {
        this.setParent(parent);
        this.setFunction(parent.getFunction());
    }

    public BasicBlock getAppendBlock() {
        return appendBlock;
    }
    public void setAppendBlock(BasicBlock appendBlock) {
        this.appendBlock = appendBlock;
    }

    public BuildEnv getParent() {
        return parent;
    }
    public void setParent(BuildEnv parent) {
        this.parent = parent;
    }

    public Function getFunction() {
        return function;
    }
    public void setFunction(Function function) {
        this.function = function;
    }


    public BuildEnv makeAppendEnv(BasicBlock appendBlock) {
        if (getAppendBlock() == appendBlock) {
            return this;
        }
        BuildEnv env = new BuildEnv(this);
        env.setAppendBlock(appendBlock);
        return env;
    }
}

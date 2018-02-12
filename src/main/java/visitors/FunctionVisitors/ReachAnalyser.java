package visitors.FunctionVisitors;

import Types.Function;
import cfg.BasicBlock;
import visitors.FunctionVisitors.FunctionVisitor;

import java.util.*;

public class ReachAnalyser implements FunctionVisitor {


    @Override
    public boolean visit(Function function) {
        Set<BasicBlock> visitedSet = new HashSet<>();
        Queue<BasicBlock> blockQueue = new LinkedList<>();

        blockQueue.add(function.getEntryBlock());

        while (blockQueue.isEmpty() == false) {
            BasicBlock block = blockQueue.poll();
            visitedSet.add(block);

            for (BasicBlock succ : block.getSuccessors()) {
                if (visitedSet.contains(succ) == false) {
                    blockQueue.add(succ);
                }
            }
        }

        List<BasicBlock> unreachableBlocks = new LinkedList<>();
        for (BasicBlock block : function.getBlocks()) {
            if (!visitedSet.contains(block)) {
                for (BasicBlock succ : block.getSuccessors()) {
                    succ.removePredecessor(block);
                }
                unreachableBlocks.add(block);
            }
        }

        for (BasicBlock block : unreachableBlocks) {
            function.getBlocks().remove(block);
            block.setDead();

        }

        return false;
    }
}

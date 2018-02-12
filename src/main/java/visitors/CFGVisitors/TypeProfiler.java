package visitors.CFGVisitors;

import Types.Function;
import Types.JSType;
import Types.TypeCollection;
import Types.FunctionManager;
import cfg.BasicBlock;

import java.util.List;

public class TypeProfiler implements Profiler {
    static class FunctionProfile {
        Function function;
        JSType returnProfile = JSType.getUndefinedType();
        JSType[] paramsProfile;

        FunctionProfile(Function function) {
            this.function = function;
            this.paramsProfile = new JSType[function.getParameters().size()];
            for (int i = 0; i < paramsProfile.length; ++i) {
                paramsProfile[i] = JSType.getUndefinedType();
            }
        }

        public JSType getReturnProfile() {
            return returnProfile;
        }

        public JSType[] getParamsProfile() {
            return paramsProfile;
        }

        public void reset() {

            for (int i = 0; i < paramsProfile.length; ++i) {
                paramsProfile[i] = JSType.getUndefinedType();
            }

            returnProfile = JSType.getUndefinedType();
        }

        boolean mergeIntoFunction() {

            boolean changed = false;
            JSType[] existingProfile = function.getParamTypes();

            for (int i = 0; i < paramsProfile.length; ++i) {
                if (paramsProfile[i] != null) {
                    changed |= (existingProfile[i] != paramsProfile[i]);
                    existingProfile[i] = paramsProfile[i];
                }
            }

            if (returnProfile != null) {
                changed |= (function.getReturnType() != returnProfile);
                function.setReturnType(returnProfile);
            }

            return changed;
        }
    }

    FunctionProfile[] profiles;


    public TypeProfiler() {
        this.profiles = new FunctionProfile[FunctionManager.getFunctionCount()];

        FunctionManager.forEach(f -> {
            profiles[f.getFuncID()] = new FunctionProfile(f);
            return false;
        });

    }

    @Override
    public boolean startProfiling() {

        for (FunctionProfile fp : profiles) {
            fp.reset();
        }
        return false;
    }

    @Override
    public boolean finishProfiling() {
        boolean changed = false;

        changed |= mergeProfiles();

        // changed |= handleEliminatedBlocks();

        return changed;
    }

    @Override
    public void profileFunction(Function function, List<JSType> profiledParams) {
        FunctionProfile fProfile = getProfileForFunction(function);
        JSType[] existingProfile = fProfile.getParamsProfile();

        int limit = Math.min(existingProfile.length, profiledParams.size());

        for (int i = 0; i < limit; ++i) {
            existingProfile[i] = mergeProfile(existingProfile[i], profiledParams.get(i));
        }
    }

    @Override
    public void profileReturn(Function function, JSType type) {
        FunctionProfile profile = getProfileForFunction(function);
        profile.returnProfile = mergeProfile(profile.returnProfile, type);
    }

    @Override
    public void reportDeadBlock(BasicBlock block) {

    }

    static private JSType mergeProfile(JSType oldType, JSType newType) {
        if (oldType == JSType.getUndefinedType() || oldType == null) {
            return newType;
        } else {
            return JSType.mergeTypes(oldType, newType);
        }
    }

    private FunctionProfile getProfileForFunction(Function function) {
        return profiles[function.getFuncID()];
    }

    boolean mergeProfiles() {

        boolean changed = false;

        for (FunctionProfile profile : profiles) {
            changed |= profile.mergeIntoFunction();
        }

        return changed;
    }
}

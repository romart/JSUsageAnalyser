package visitors.CFGVisitors;

import Types.Declaration;
import Types.Function;
import Types.JSType;
import Types.TypeCollection;
import Types.FunctionManager;
import Utils.SourceLocation;
import Utils.UsageLocation;
import cfg.References.Reference;

import java.util.Collection;

public class UsageSymbolChecker implements SymbolChecker {

    Declaration declaration;
    Function currentFunction;

    Collection<UsageLocation> exactUsages;
    Collection<UsageLocation> possibelUsages;

    public UsageSymbolChecker(Collection<UsageLocation> exactUsages, Collection<UsageLocation> possibleUsages, Declaration declaration) {
        this.declaration = declaration;
        this.exactUsages = exactUsages;
        this.possibelUsages = possibleUsages;
    }

    @Override
    public void checkSymbol(Reference reference, JSType type, TypeCollection impossibleTypes, SourceLocation location) {

        if (type == null) return;

        if (declaration.getName().equals(reference.getName())) {
            if (declaration.getType() == Declaration.DeclarationType.Function) {
                if (reference.getType() == Reference.Type.FUNCTION) {
                    Function function = FunctionManager.getFunction(reference.getName()).get();
                    registerExactUsage(UsageLocation.makeFunctionUsage(function, location));
                }
            } else {
                if (!declaration.getJsType().isSubtypeOfAny(impossibleTypes)) {
                    if (couldBePropertyUse(reference.getName(), declaration.getJsType(), type)) {
                        registerExactUsage(UsageLocation.makeExactUsage(declaration.getJsType(), reference.getName(), location));
                    } else {
                        if (couldBePossibleUsage(reference.getName(), type)) {
                            registerPossibleUsage(UsageLocation.makePossibleUsage(declaration.getJsType(), reference.getName(), location));
                        }
                    }
                }
            }
        }
    }

    private boolean couldBePossibleUsage(String name, JSType type) {
        JSType declType = declaration.getJsType();
        if (declType.isSubtypeOf(type)) {
            return true;
        }

        if (type == JSType.getUndefinedType() || type == JSType.getObjectType()) {
            return true;
        }

        return false;
    }

    private void registerPossibleUsage(UsageLocation usageLocation) {

        if (exactUsages.contains(usageLocation)) return;
        if (possibelUsages.contains(usageLocation)) return;

        possibelUsages.add(usageLocation);
    }

    private void registerExactUsage(UsageLocation usageLocation) {
        if (possibelUsages.contains(usageLocation)) {
            possibelUsages.remove(usageLocation);
        }

        if (!exactUsages.contains(usageLocation)) {
            exactUsages.add(usageLocation);
        }

    }

    @Override
    public void setupFunction(Function function) {
        currentFunction = function;
    }

    private static boolean couldBePropertyUse(String propertyName, JSType declType, JSType type) {
        JSType currentType = type;
        if (currentType.isSubtypeOf(declType)) {
            while (currentType != declType) {
                if (currentType.hasProperty(propertyName)) {
                    break;
                }
                currentType = currentType.getSuperType();
            }

            if (currentType == declType) {
                return true;
            }

        }
        return false;
    }
}

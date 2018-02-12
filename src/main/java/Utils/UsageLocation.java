package Utils;

import Types.Function;
import Types.JSType;

public class UsageLocation implements Comparable<UsageLocation> {

    private SourceLocation location;

    private UsageLocation(SourceLocation location) {
        this.location = location;
    }

    @Override
    public int compareTo(UsageLocation o) {
        long lineDiff = getLocation().getLineNo() - o.getLocation().getLineNo();
        if (lineDiff != 0) {
            return lineDiff < 0 ? -1 : 1;
        }

        long posDiff = getLocation().getLinePos() - o.getLocation().getLinePos();
        return posDiff == 0 ? 0 : posDiff < 0 ? -1 : 1;

    }

    public SourceLocation getLocation() {
        return location;
    }


    public static UsageLocation makeFunctionUsage(Function function, SourceLocation location) {
        return new FunctionUsage(location, function);
    }

    public static UsageLocation makeExactUsage(JSType type, String propertyName, SourceLocation location) {
        return new PropertyUsage(type, propertyName, PropertyUsage.UsageType.EXACT, location);
    }

    public static UsageLocation makePossibleUsage(JSType type, String propertyName, SourceLocation location) {
        return new PropertyUsage(type, propertyName, PropertyUsage.UsageType.POSSIBLE, location);
    }

    static class FunctionUsage extends UsageLocation {
        private Function function;

        private FunctionUsage(SourceLocation location, Function function) {
            super(location);
            this.function = function;
        }

        @Override
        public String toString() {
            return String.format("Function usage:\t\t%s is used at %s", function, getLocation().toString());
        }
    }

    static class PropertyUsage extends UsageLocation {

        enum UsageType {
            EXACT,
            POSSIBLE
        }

        private UsageType usageType;
        private JSType jsType;
        private String propertyName;

        private PropertyUsage(JSType jsType, String propertyName, UsageType uType, SourceLocation location) {
            super(location);
            this.usageType = uType;
            this.jsType = jsType;
            this.propertyName = propertyName;
        }

        @Override
        public String toString() {
            return String.format("%s usage:\t\t[%s].%s at %s", usageType, jsType, propertyName, getLocation().toString());
        }
    }

}

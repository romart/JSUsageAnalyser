package Types;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

public class Declaration {

    public Function getFunction() {
        return function;
    }

    public enum DeclarationType{
        // in case foo()
        Function,

        // in case A.foo
        Field,
        MemberFunction,

        // in case A.prototype.foo
        TypeField,
        TypeMemberFunction,
    }

    static final String PROTOTYPE = "prototype";

    DeclarationType type;

    public JSType getJsType() {
        return jsType;
    }

    JSType jsType;
    String name;

    Function function;

    String declarationString;

    public void flipToFunction() {
        if (type == DeclarationType.TypeField) {
            type = DeclarationType.TypeMemberFunction;
        } else if (type == DeclarationType.Field) {
            type = DeclarationType.MemberFunction;
        }
    }

    public DeclarationType getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public String getDeclarationString() {
        return declarationString;
    }

    private Declaration(String declarationString, String name, JSType jsType, Function function, DeclarationType type) {
        this.declarationString = declarationString;
        this.name = name;
        this.jsType = jsType;
        this.function = function;
        this.type = type;
    }

    public static Declaration parse(String declarationString) throws ParseException {
        JSType jsType = null;
        String name = null;
        DeclarationType type;
        Function function = null;
        if (declarationString.indexOf('.') != -1) {
            String[] tokens = declarationString.split("\\.");
            if (tokens.length == 3) {
                // probably KLASS.prototype.NAME
                if (tokens[1].equals(PROTOTYPE)) {
                    jsType = TypeManager.getType(tokens[0]);
                    name = tokens[2];

                    if (!jsType.hasProperty(name)) {
                        throw new ParseException("No such field found: \"" + name + "\"", declarationString.length() - name.length());
                    }
                    type = DeclarationType.Field;
                } else {
                    throw new ParseException("Syntax error in \"" + declarationString + "\"", tokens[0].length() + 1);
                }
            } else {
                throw new ParseException("Unknown declaration format: \"" + declarationString + "\"", tokens[0].length() + 1);
            }
        } else {
            name = declarationString;
            type = DeclarationType.Function;
            Optional<Function> f = FunctionManager.getFunction(name);
            if (f.isPresent()) {
                function = f.get();
            } else {
                throw new ParseException("Not such function existed: \"" + declarationString + "\"", 0);
            }
        }

        return new Declaration(declarationString, name, jsType, function, type);
    }
}

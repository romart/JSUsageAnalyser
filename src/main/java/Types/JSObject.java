package Types;

import java.util.LinkedList;
import java.util.List;

public class JSObject {

    JSType type; // if known
    List<JSType> possibleTypes = new LinkedList<>();
}

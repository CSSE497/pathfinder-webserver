package dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class Quantity {

    private static int varIndex = 1;
    private final String varName;
    private final List<String> variableDeclarations;
    private final List<String> constraints;

    private Quantity(String varName) {
        this(varName, new ArrayList<>(), new ArrayList<>());
    }

    private Quantity(String varName, List<String> variableDeclarations, List<String> constraints) {
        this.varName = varName;
        this.variableDeclarations = variableDeclarations;
        this.constraints = constraints;
    }

    synchronized static String nextVarName() {
        return String.format("_tmp%d", varIndex++);
    }

    static Quantity parse(Object quantity, Map<String, Context.Model> entities) {
        if (quantity instanceof Integer) {
            quantity = quantity.toString();
        }
        if (quantity instanceof String) {
            return parseString((String) quantity, entities);
        } else if (quantity instanceof Map) {
            return parseMap((Map) quantity, entities);
        } else {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
    }

    private static Quantity parseString(String quantity, Map<String, Context.Model> entities) {
        String[] parts = quantity.split("\\.");
        if (parts.length == 1) {
            // now => now
            return new Quantity(parts[0]);
        } else if (parts.length == 2 && entities.containsKey(parts[0])) {
            if (entities.get(parts[0]).keywords().contains(parts[1])) {
                // c1.dropoff_time => dropoff_time[c1]
                return new Quantity(String.format("%s[%s]", parts[1], parts[0]));
            } else {
                // c1.request_time => parameters["request_time"][c1]
                return new Quantity(String.format("parameters[\"%s\"][%s]", parts[1], parts[0]));
            }
        } else {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
    }

    private static Quantity parseMap(Map<String, List<Object>> quantity,
        Map<String, Context.Model> entities) {
        if (quantity.size() != 1) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }

        Operator operator = Operator.valueOf(quantity.keySet().iterator().next());
        List<Object> operands = quantity.get(operator.name());
        if (operands.size() != operator.operands) {
            throw new IllegalArgumentException("Wrong number of operands for " + operator);
        }
        List<Quantity> qs = operands.stream().map(o -> parse(o, entities)).collect(toList());
        String name = nextVarName();
        String innerName = String.format("%s[%s]", name, String.join(",", entities.keySet()));
        List<String> types = entities.values().stream().map(m -> m.type()).collect(toList());
        String defName = String.format("%s[%s]", name, String.join(",", types));
        List<String> constraints = qs.stream().flatMap(q -> q.constraints.stream()).collect(toList());
        constraints.addAll(operator.constraints(innerName, qs.stream().map(Quantity::varName).collect(toList())));
        List<String> varDefs =
            qs.stream().flatMap(q -> q.variableDeclarations().stream()).collect(toList());
        varDefs.addAll(operator.variableDefinitions(defName));
        return new Quantity(innerName, varDefs, constraints);
    }

    String varName() {
        return varName;
    }

    List<String> variableDeclarations() {
        return new ArrayList<>(variableDeclarations);
    }

    List<String> constraints() {
        return new ArrayList<>(constraints);
    }
}

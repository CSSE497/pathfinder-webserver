package dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

enum Operator {
    add(2),
    subtract(2),
    divide(2),
    multiply(2),
    absolute_value(1);

    private static final Map<Operator, List<String>> constraints = new HashMap<>();
    private static final Map<Operator, List<String>> variableDefinitions = new HashMap<>();

    static {
        constraints.put(add, Arrays.asList("@addConstraint(model, %s == %s + %s)"));
        constraints.put(subtract, Arrays.asList("@addConstraint(model, %s == %s - %s)"));
        constraints.put(divide, Arrays.asList("@addConstraint(model, %s == %s / %s)"));
        constraints.put(multiply, Arrays.asList("@addConstraint(model, %s == %s * %s)"));
        constraints.put(absolute_value, Arrays.asList(
            "@addConstraint(model, %1$s == pos%1$s + neg%1$s)",
            "@addConstraint(model, %2$s == pos%1$s - neg%1$s)"));
        variableDefinitions.put(add, Arrays.asList("@defVar(model, %s, Int)"));
        variableDefinitions.put(subtract, Arrays.asList("@defVar(model, %s, Int)"));
        variableDefinitions.put(divide, Arrays.asList("@defVar(model, %s, Int)"));
        variableDefinitions.put(multiply, Arrays.asList("@defVar(model, %s, Int)"));
        variableDefinitions.put(absolute_value, Arrays.asList(
            "@defVar(model, %s, Int)",
            "@defVar(model, pos%s >= 0, Int)",
            "@defVar(model, neg%s >= 0, Int)"));
    }

    int operands;

    Operator(int operands) {
        this.operands = operands;
    }

    List<String> constraints(String varName, List<String> arguments) {
        List<String> args = new ArrayList<>();
        args.add(varName);
        args.addAll(arguments);
        return constraints.get(this).stream().map(s -> String.format(s, args.toArray()))
            .collect(toList());
    }

    List<String> variableDefinitions(String varName) {
        return variableDefinitions.get(this).stream().map(s -> String.format(s, varName))
            .collect(toList());
    }
}

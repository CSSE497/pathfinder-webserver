package dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ObjectiveFunction {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final String raw;
    private final String compiled;

    private ObjectiveFunction(String raw, String compiled) {
        this.raw = raw;
        this.compiled = compiled;
    }

    public static ObjectiveFunction compile(String raw) throws IOException {
        DSL dsl = mapper.readValue(raw, DSL.class);
        Map<String, Context.Model> entities = dsl.context.entities;
        StringBuffer buffer = new StringBuffer();
        buffer.append(dsl.sense.declaration());
        Quantity q = Quantity.parse(dsl.quantity, entities);
        q.variableDeclarations().forEach(vd -> { buffer.append(vd); buffer.append('\n'); });
        if (!q.constraints().isEmpty() || dsl.context.method == Context.Method.max) {
            entities.forEach((k, v) -> {
                buffer.append(String.format("for %s in %s\n", k, v.type()));
            });
            q.constraints().forEach(c -> {
                buffer.append(c);
                buffer.append('\n');
            });
            if (dsl.context.method == Context.Method.max) {
                buffer.append(String.format("@addConstraint(model, _value >= %s)\n", q.varName()));
            }
            entities.forEach((k, v) -> {
                buffer.append("end\n");
            });
        }
        if (dsl.context.method == Context.Method.sum) {
            buffer.append(String.format(
                "@addConstraint(model, _value == sum{%s,%s})\n", q.varName(),
                String.join(",", entities.keySet().stream().map(k ->
                    k + "=" + entities.get(k).type()).collect(toList()))));
        }
        buffer.append(dsl.sense.objective());
        return new ObjectiveFunction(raw, buffer.toString());
    }

    public String getCompiled() {
        return compiled;
    }

    public String getRaw() {
        return raw;
    }
}

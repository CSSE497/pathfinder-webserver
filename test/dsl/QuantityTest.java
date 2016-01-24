package dsl;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuantityTest {
    private final Map<String, Context.Model> entities = new HashMap<>();

    @Before public void setup() throws NoSuchFieldException, IllegalAccessException {
        entities.put("c1", Context.Model.commodity);
        entities.put("c2", Context.Model.commodity);
        Field field = Quantity.class.getDeclaredField("varIndex");
        field.setAccessible(true);
        field.set(null, 1);
    }

    @Test public void testKeyword() {
        String leaf = "c1.dropoff_time";
        Quantity q = Quantity.parse(leaf, entities);
        assertTrue(q.constraints().isEmpty());
        assertTrue(q.variableDeclarations().isEmpty());
        assertEquals("dropoff_time[c1]", q.varName());
    }

    @Test public void testParameter() {
        String leaf = "c1.chimneys";
        Quantity q = Quantity.parse(leaf, entities);
        assertTrue(q.constraints().isEmpty());
        assertTrue(q.variableDeclarations().isEmpty());
        assertEquals("parameters[\"chimneys\"][c1]", q.varName());
    }

    @Test public void testConstant() {
        String leaf = "now";
        Quantity q = Quantity.parse(leaf, entities);
        assertTrue(q.constraints().isEmpty());
        assertTrue(q.variableDeclarations().isEmpty());
        assertEquals("now", q.varName());
    }

    @Test public void testSubtract() {
        Map<String, List<String>> subtractNode = new HashMap<>();
        subtractNode.put("subtract", Arrays.asList("c1.dropoff_time", "c1.request_time"));
        Quantity q = Quantity.parse(subtractNode, entities);
        assertEquals("_tmp1[c1,c2]", q.varName());
        assertEquals(1, q.variableDeclarations().size());
        assertEquals("@defVar(model, _tmp1[DA,DA], Int)", q.variableDeclarations().get(0));
        assertEquals(1, q.constraints().size());
        String constraint =
            "@addConstraint(model, _tmp1[c1,c2] == dropoff_time[c1] - parameters[\"request_time\"][c1])";
        assertEquals(constraint, q.constraints().get(0));
    }
}

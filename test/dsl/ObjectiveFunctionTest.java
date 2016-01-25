package dsl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.lang.reflect.Field;

import play.api.data.ObjectMapping;

import static junit.framework.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ObjectiveFunctionTest {

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        Field field = Quantity.class.getDeclaredField("varIndex");
        field.setAccessible(true);
        field.set(null, 1);
    }

    @Test
    public void testMinimizeTotalDistance() throws IOException {
        String dsl = ""
            + "sense: min\n"
            + "context:\n"
            + "    method: sum\n"
            + "    for:\n"
            + "        t: transport\n"
            + "quantity: t.distance";
        String expected = ""
            + "@defVar(model, _value, Int)\n"
            + "@addConstraint(model, _value == sum{distance[t],t=TA})\n"
            + "@setObjective(model, Min, _value)";
        assertEquals(expected, ObjectiveFunction.compile(dsl).getCompiled());
    }

    @Test
    public void testMinimizeTotalTime() throws IOException {
        String dsl = ""
            + "sense: min\n"
            + "context:\n"
            + "    method: sum\n"
            + "    for:\n"
            + "        t: transport\n"
            + "quantity: t.duration";
        String expected = ""
            + "@defVar(model, _value, Int)\n"
            + "@addConstraint(model, _value == sum{duration[t],t=TA})\n"
            + "@setObjective(model, Min, _value)";
        assertEquals(expected, ObjectiveFunction.compile(dsl).getCompiled());
    }

    @Test
    public void testMinimizeMaxTransportDistance() throws IOException {
        String dsl = ""
            + "sense: min\n"
            + "context:\n"
            + "    method: max\n"
            + "    for:\n"
            + "        t: transport\n"
            + "quantity: t.distance";
        String expected = ""
            + "@defVar(model, _value, Int)\n"
            + "for t in TA\n"
            + "@addConstraint(model, _value >= distance[t])\n"
            + "end\n"
            + "@setObjective(model, Min, _value)";
        assertEquals(expected, ObjectiveFunction.compile(dsl).getCompiled());
    }

    @Test
    public void testMinimizeTotalFuel() throws IOException {
        String dsl = ""
            + "sense: min\n"
            + "context:\n"
            + "    method: max\n"
            + "    for:\n"
            + "        t: transport\n"
            + "quantity:\n"
            + "    divide:\n"
            + "        - t.distance\n"
            + "        - t.mpg";
        String expected = ""
            + "@defVar(model, _value, Int)\n"
            + "@defVar(model, _tmp1[TA], Int)\n"
            + "for t in TA\n"
            + "@addConstraint(model, _tmp1[t] == distance[t] / parameters[\"mpg\"][t])\n"
            + "@addConstraint(model, _value >= _tmp1[t])\n"
            + "end\n"
            + "@setObjective(model, Min, _value)";
        assertEquals(expected, ObjectiveFunction.compile(dsl).getCompiled());
    }

    @Test
    public void testMinSumDiffDropoffRequestTime() throws IOException {
        String dsl = ""
            + "sense: min\n"
            + "context:\n"
            + "  method: sum\n"
            + "  for:\n"
            + "    c1: commodity\n"
            + "    c2: commodity\n"
            + "quantity:\n"
            + "  absolute_value:\n"
            + "    - subtract:\n"
            + "      - subtract:\n"
            + "        - c1.dropoff_time\n"
            + "        - c1.request_time\n"
            + "      - subtract:\n"
            + "        - c2.dropoff_time\n"
            + "        - c2.request_time";
        String expected = ""
            + "@defVar(model, _value, Int)\n"
            + "@defVar(model, _tmp1[DA,DA], Int)\n"
            + "@defVar(model, _tmp2[DA,DA], Int)\n"
            + "@defVar(model, _tmp3[DA,DA], Int)\n"
            + "@defVar(model, _tmp4[DA,DA], Int)\n"
            + "@defVar(model, pos_tmp4[DA,DA] >= 0, Int)\n"
            + "@defVar(model, neg_tmp4[DA,DA] >= 0, Int)\n"
            + "for c1 in DA\n"
            + "for c2 in DA\n"
            + "@addConstraint(model, _tmp1[c1,c2] == dropoff_time[c1] - parameters[\"request_time\"][c1])\n"
            + "@addConstraint(model, _tmp2[c1,c2] == dropoff_time[c2] - parameters[\"request_time\"][c2])\n"
            + "@addConstraint(model, _tmp3[c1,c2] == _tmp1[c1,c2] - _tmp2[c1,c2])\n"
            + "@addConstraint(model, _tmp4[c1,c2] == pos_tmp4[c1,c2] + neg_tmp4[c1,c2])\n"
            + "@addConstraint(model, _tmp3[c1,c2] == pos_tmp4[c1,c2] - neg_tmp4[c1,c2])\n"
            + "end\n"
            + "end\n"
            + "@addConstraint(model, _value == sum{_tmp4[c1,c2],c1=DA,c2=DA})\n"
            + "@setObjective(model, Min, _value)";
        assertEquals(expected, ObjectiveFunction.compile(dsl).getCompiled());
    }

    @Test
    public void testMinMaxDiffDropoffRequestTime() throws IOException {
        String dsl = ""
            + "sense: min\n"
            + "context:\n"
            + "  method: max\n"
            + "  for:\n"
            + "    c1: commodity\n"
            + "    c2: commodity\n"
            + "quantity:\n"
            + "  absolute_value:\n"
            + "    - subtract:\n"
            + "      - subtract:\n"
            + "        - c1.dropoff_time\n"
            + "        - c1.request_time\n"
            + "      - subtract:\n"
            + "        - c2.dropoff_time\n"
            + "        - c2.request_time";
        String expected = ""
            + "@defVar(model, _value, Int)\n"
            + "@defVar(model, _tmp1[DA,DA], Int)\n"
            + "@defVar(model, _tmp2[DA,DA], Int)\n"
            + "@defVar(model, _tmp3[DA,DA], Int)\n"
            + "@defVar(model, _tmp4[DA,DA], Int)\n"
            + "@defVar(model, pos_tmp4[DA,DA] >= 0, Int)\n"
            + "@defVar(model, neg_tmp4[DA,DA] >= 0, Int)\n"
            + "for c1 in DA\n"
            + "for c2 in DA\n"
            + "@addConstraint(model, _tmp1[c1,c2] == dropoff_time[c1] - parameters[\"request_time\"][c1])\n"
            + "@addConstraint(model, _tmp2[c1,c2] == dropoff_time[c2] - parameters[\"request_time\"][c2])\n"
            + "@addConstraint(model, _tmp3[c1,c2] == _tmp1[c1,c2] - _tmp2[c1,c2])\n"
            + "@addConstraint(model, _tmp4[c1,c2] == pos_tmp4[c1,c2] + neg_tmp4[c1,c2])\n"
            + "@addConstraint(model, _tmp3[c1,c2] == pos_tmp4[c1,c2] - neg_tmp4[c1,c2])\n"
            + "@addConstraint(model, _value >= _tmp4[c1,c2])\n"
            + "end\n"
            + "end\n"
            + "@setObjective(model, Min, _value)";
        assertEquals(expected, ObjectiveFunction.compile(dsl).getCompiled());
    }

}

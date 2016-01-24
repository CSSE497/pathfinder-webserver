package dsl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Context {

    @JsonProperty Method method;
    @JsonProperty(value = "for") Map<String, Model> entities;


    enum Method {
        sum, max, min
    }


    enum Model {
        transport, commodity;

        private static final Map<Model, List<String>> KEYWORDS = new HashMap<>();
        private static final Map<Model, String> TYPES = new HashMap<>();
        static {
            KEYWORDS.put(transport, Arrays.asList("distance", "duration"));
            KEYWORDS.put(commodity, Arrays.asList("pickup_time", "dropoff_time", "distance"));
            TYPES.put(transport, "TA");
            TYPES.put(commodity, "DA");
        }

        List<String> keywords() {
            return KEYWORDS.get(this);
        }

        String type() {
            return TYPES.get(this);
        }
    }
}

package dsl;

import com.fasterxml.jackson.annotation.JsonProperty;

class DSL {

    @JsonProperty Sense sense;
    @JsonProperty Context context;
    @JsonProperty Object quantity;


    enum Sense {
        min("Min"),
        max("Max");

        private final String sense;

        Sense(String sense) {
            this.sense = sense;
        }

        String objective() {
            return "@setObjective(model, " + sense + ", _value)";
        }

        String declaration() {
            return "@defVar(model, _value, Int)\n";
        }
    }
}

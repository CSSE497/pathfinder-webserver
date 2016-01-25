package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity public class ObjectiveFunction extends Model {
    public static final String MIN_DIST = "MIN_DIST";
    public static final String MIN_TIME = "MIN_TIME";
    public static final Model.Find<String, ObjectiveFunction> find =
        new Model.Find<String, ObjectiveFunction>() {
        };

    @Id @NotNull public String id;
    @NotNull public String function;
    @NotNull public String dsl;

}

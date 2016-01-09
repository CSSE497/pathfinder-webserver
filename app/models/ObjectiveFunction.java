package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity public class ObjectiveFunction extends Model {
    public static final String MIN_DIST = "MIN_DIST";
    public static final String MIN_TIME = "MIN_TIME";
    public static final Find<String, ObjectiveFunction> find =
        new Find<String, ObjectiveFunction>() {
        };

    @Id @NotNull public String id;
    @NotNull public String function;

}

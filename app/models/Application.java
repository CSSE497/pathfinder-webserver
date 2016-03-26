package models;

import com.avaje.ebean.Model;

import java.security.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity public class Application extends Model {

    public static final int REQUIRED_CREATE_FIELDS = 1;
    public static final int TOKEN_LENGTH = 255;
    public static final Model.Find<String, Application> find =
        new Model.Find<String, Application>() {
        };

    @Id @NotNull(message = "Id cannot be null") public String id;

    @NotNull(message = "Application name was not provided") public String name;

    public byte[] key;

    @ManyToOne public Customer customer;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL) public List<CapacityParameter>
        capacityParameters;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL) public List<ObjectiveParameter>
        objectiveParameters;

    @NotNull @ManyToOne public ObjectiveFunction objectiveFunction;

    @Version public Timestamp lastUpdate;
}

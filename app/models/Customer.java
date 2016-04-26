package models;

import com.avaje.ebean.Model;

import java.security.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity public class Customer extends Model {

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final Model.Find<String, Customer> find = new Model.Find<String, Customer>() {
    };

    @Id @NotNull public String email;
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) public List<Application>
        applications;
    @Version public Timestamp lastUpdate;
}

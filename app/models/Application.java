package models;

import com.avaje.ebean.Model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity public class Application extends Model {

    public static final int REQUIRED_CREATE_FIELDS = 1;
    public static final int TOKEN_LENGTH = 255;
    public static Find<UUID, Application> find = new Find<UUID, Application>() {
    };
    @Id @NotNull(message = "Id cannot be null") public UUID id;
    @NotNull(message = "Application name was not provided") public String name;
    @NotNull(message = "Application must be associated to a customer") public String email;
    @NotNull(message = "Cluster id not present") public long clusterId;
}

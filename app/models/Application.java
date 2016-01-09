package models;

import com.avaje.ebean.Model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity public class Application extends Model {

    public static final int NAME_MIN_LENGTH = 1;
    public static final int REQUIRED_CREATE_FIELDS = 1;
    public static final int TOKEN_LENGTH = 255;
    public static Find<UUID, Application> find = new Find<UUID, Application>() {
    };
    @Id @NotNull(message = "Id cannot be null") public UUID id;
    @NotNull(message = "Application token not present") public byte[] token;
    @NotNull(message = "Application name was not provided")
    @Size(min = NAME_MIN_LENGTH, message = "Application name was too short, it must be at least "
        + NAME_MIN_LENGTH + " characters") public String name;
    @NotNull(message = "Cluster id not present") @Min(value = 1, message = "Invalid cluster id")
    public long clusterId;
}

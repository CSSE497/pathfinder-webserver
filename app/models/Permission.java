package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.DbJson;

import java.util.Map;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Permission extends Model {

    public static final Model.Find<PermissionKey, Permission> find =
        new Model.Find<PermissionKey, Permission>() { };

    @EmbeddedId
    public PermissionKey key;

    @DbJson
    public Map<String, Object> permissions;
}

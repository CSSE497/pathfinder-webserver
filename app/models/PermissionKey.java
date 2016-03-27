package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PermissionKey implements Serializable {

    public String email;

    @Column(name = "application_id")
    public String applicationId;

    @Override
    public int hashCode() {
        return (email == null ? 0 : email.hashCode())
            + (applicationId == null ? 0 : applicationId.hashCode());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PermissionKey) {
            PermissionKey otherKey =  (PermissionKey) other;
            return email.equals(otherKey.email) && applicationId.equals(otherKey.applicationId);
        } else {
            return false;
        }
    }
}

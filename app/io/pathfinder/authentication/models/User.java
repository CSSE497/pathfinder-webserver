package io.pathfinder.authentication.models;

import com.avaje.ebean.Model;
import org.hibernate.validator.constraints.Email;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class User extends Model{

  public static final int userTokenLength = 256;
  public static final int passwordMinLength = 6;

  @Id
  @NotNull
  @Email
  public String email;

  @NotNull
  @Size(min = passwordMinLength)
  public String password;

  @NotNull
  @Size(min = userTokenLength, max = userTokenLength)
  public String userToken;

  public static Find<String, User> find = new Find<String, User>(){};
}

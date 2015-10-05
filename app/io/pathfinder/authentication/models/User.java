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
  @NotNull(message = "Email was not provided")
  @Email(message = "Invalid email address")
  public String email;

  @NotNull(message = "Password was not provided")
  @Size(min = passwordMinLength, message = "Password was to short, it must be at least " + passwordMinLength + " characters")
  public String password;

  @NotNull(message = "Token was not provided")
  @Size(min = userTokenLength, max = userTokenLength, message = "Token must be " + userTokenLength + " characters")
  public String userToken;

  public static Find<String, User> find = new Find<String, User>(){};
}

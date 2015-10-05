package io.pathfinder.authentication.models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model{

  public static final int userTokenLength = 256;
  public static final int passwordMinLength = 6;

  @Id
  @Constraints.Required(message = "Email is required")
  @Constraints.Email(message = "Email is not valid")
  public String email;

  @Constraints.Required(message = "Password is required")
  @Constraints.Max(value = 6, message = "Password must be at least " + passwordMinLength + " characters.")
  public String password;

  @Constraints.Required(message = "User token is required")
  @Constraints.MaxLength(value = userTokenLength, message = "User token must be " + userTokenLength + " bytes, over " + userTokenLength + " bytes")
  @Constraints.MinLength(value = userTokenLength, message = "User token must be " + userTokenLength + " bytes, under " + userTokenLength + " bytes")
  public String userToken;

  public static Find<String, User> find = new Find<String, User>(){};
}

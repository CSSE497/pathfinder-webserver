package io.pathfinder.models;

import com.avaje.ebean.Model;
import org.hibernate.validator.constraints.Email;

import javax.persistence.Entity;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class PathfinderUser extends Model{

  public static final int USER_TOKEN_LENGTH = 256;
  public static final int USER_PASSWORD_MIN_LENGTH = 6;

  @Id
  @NotNull(message = "Username was not provided")
  public String username;

  @NotNull(message = "Password was not provided")
  @Size(min = USER_PASSWORD_MIN_LENGTH, message = "Password was too short, it must be at least " + USER_PASSWORD_MIN_LENGTH + " characters")
  public String password;

  @NotNull(message = "Token was not provided")
  @Size(min = USER_TOKEN_LENGTH, max = USER_TOKEN_LENGTH, message = "Token must be " + USER_TOKEN_LENGTH + " characters")
  public String userToken;

  public static Find<String, PathfinderUser> find = new Find<String, PathfinderUser>(){};
}

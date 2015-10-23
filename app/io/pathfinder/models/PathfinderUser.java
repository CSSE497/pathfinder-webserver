package io.pathfinder.models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class PathfinderUser extends Model{

  public static final int TOKEN_LENGTH = 256;
  public static final int PASSWORD_MIN_LENGTH = 6;
  public static final int REQUIRED_CREATE_FIELDS = 3;

  @Id
  @NotNull(message = "Username was not provided")
  public String username;

  @JsonIgnore
  @NotNull(message = "Password was not provided")
  @Size(min = PASSWORD_MIN_LENGTH, message = "Password was too short, it must be at least " + PASSWORD_MIN_LENGTH + " characters")
  public String password;

  @NotNull(message = "Token was not provided")
  @Size(min = TOKEN_LENGTH, max = TOKEN_LENGTH, message = "Token must be " + TOKEN_LENGTH + " characters")
  public String userToken;

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String pass) {
    password = pass;
  }

  public static Find<String, PathfinderUser> find = new Find<String, PathfinderUser>(){};
}

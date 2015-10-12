package io.pathfinder.application.models;

import com.avaje.ebean.Model;
import org.hibernate.validator.constraints.Email;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Application extends Model {

  public static final int APPLICATION_TOKEN_LENGTH = 256;
  public static final int APPLICATION_NAME_MIN_LENGTH = 1;

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public long applicationId;

  @NotNull(message = "Application name was not provided")
  @Size(min = APPLICATION_NAME_MIN_LENGTH, message = "Application name was too short, it must be at least " + APPLICATION_NAME_MIN_LENGTH + " characters")
  public String name;

  @NotNull(message = "Token was not provided")
  @Size(min = APPLICATION_TOKEN_LENGTH, max = APPLICATION_TOKEN_LENGTH, message = "Token must be " + APPLICATION_TOKEN_LENGTH + " characters")
  public String applicationToken;
}

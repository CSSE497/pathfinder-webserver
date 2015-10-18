package io.pathfinder.pathfinderapplication.models;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class PathfinderApplication extends Model {

  public static final int REQUIRED_CREATE_FIELDS = 1;
  public static final int NAME_MIN_LENGTH = 1;

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public long applicationId;

  @NotNull(message = "Application name was not provided")
  @Size(min = NAME_MIN_LENGTH, message = "Application name was too short, it must be at least " + NAME_MIN_LENGTH + " characters")
  public String name;

  public Long clusterId;

  public static Find<Long, PathfinderApplication> find = new Find<Long, PathfinderApplication>(){};
}

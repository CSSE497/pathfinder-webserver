package io.pathfinder.models;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Vehicle extends Model {
  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public long id;

  @Column(nullable=false)
  public double latitude;

  @Column(nullable=false)
  public double longitude;

  @Column(nullable=false)
  public int capacity;

  @ManyToOne
  @JoinColumn
  public Cluster cluster;
}

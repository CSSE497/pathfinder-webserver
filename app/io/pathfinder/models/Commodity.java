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
public class Commodity extends Model {
  @Id
  @Column(name="id", nullable=false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public long id;

  @Column(name="startLatitude", nullable=false)
  public double startLatitude;

  @Column(name="startLongitude", nullable=false)
  public double startLongitude;

  @Column(name="endLatitude", nullable=false)
  public double endLatitude;

  @Column(name="endLongitude", nullable=false)
  public double endLongitude;

  @Column(name = "param")
  public int param;

  @JoinColumn
  @ManyToOne
  public Cluster cluster;
}

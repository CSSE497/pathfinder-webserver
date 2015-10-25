package io.pathfinder.models;

import com.avaje.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Cluster extends Model {
  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public long id;

  @Column
  public String authenticationToken;

  @OneToMany(mappedBy = "cluster", cascade=CascadeType.ALL)
  public List<Vehicle> vehicleList;

  @OneToMany(mappedBy = "cluster", cascade=CascadeType.ALL)
  public List<Commodity> commodityList;

  public static Find<Long, Cluster> find = new Find<Long, Cluster>(){};
}

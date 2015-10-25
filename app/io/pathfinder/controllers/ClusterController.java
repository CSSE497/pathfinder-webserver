package io.pathfinder.controllers;

import io.pathfinder.models.Cluster;
import io.pathfinder.util.Security;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class ClusterController extends Controller{
  public static Cluster createDefaultCluster() {
    Cluster cluster = new Cluster();
    cluster.authenticationToken = Security.generateToken(128);
    cluster.save();
    return cluster;
  }
}

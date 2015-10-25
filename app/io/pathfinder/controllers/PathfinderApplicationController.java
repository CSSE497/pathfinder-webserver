package io.pathfinder.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.pathfinder.annotations.interfaces.RequireJson;
import io.pathfinder.models.PathfinderApplication;
import io.pathfinder.util.Security;

import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;

public class PathfinderApplicationController extends Controller {

  Config config = ConfigFactory.load();

  @Inject
  WSClient ws;

  @RequireJson()
  public Promise<Result> createApplication() {
    JsonNode jsonNode = request().body().asJson();
    if (!jsonNode.isObject()) {
      return Promise.pure(badRequest("Json sent was not a Json objects"));
    }

    int size = jsonNode.size();
    if (size > PathfinderApplication.REQUIRED_CREATE_FIELDS) {
      return Promise.pure(badRequest("Too many fields were detected in the application create request"));
    }

    if (size < PathfinderApplication.REQUIRED_CREATE_FIELDS) {
      return Promise.pure(badRequest("Too few fields were detected in the application create request"));
    }

    if(!jsonNode.has("name")) {
      return Promise.pure(badRequest("Json did not include a name field"));
    }

    PathfinderApplication application = Json.fromJson(jsonNode, PathfinderApplication.class);

    String pathfinderServerURL = config.getString("pathfinder.server.url");
    WSRequest clusterCreateRequest = ws.url(pathfinderServerURL + "/cluster");

    JsonNode clusterNode = Json.newObject();
    return clusterCreateRequest.post(clusterNode).map(new CreateApplicationResponse(application));
  }

  private class CreateApplicationResponse implements Function<WSResponse, Result>{

    private PathfinderApplication application;

    public CreateApplicationResponse(PathfinderApplication application) {
      this.application = application;
    }

    @Override
    public Result apply(WSResponse response) throws Throwable {
      if (response.getStatus() != Http.Status.CREATED) {
        return badRequest(response.asJson());
      }

      application.clusterId = response.asJson().findValue("id").longValue();
      application.token = Security.generateToken(PathfinderApplication.TOKEN_LENGTH).getBytes();

      do {
        application.id = UUID.randomUUID();
      } while (PathfinderApplication.find.byId(application.id) != null);

      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();
      Set<ConstraintViolation<PathfinderApplication>> violations = validator.validate(application);

      if(violations.size() == 0) {
        application.save();
        return ok(Json.toJson(application));
      } else {
        String pathfinderServerURL = config.getString("pathfinder.server.url");
        WSRequest clusterDeleteRequest = ws.url(pathfinderServerURL + "/cluster/" + application.clusterId);
        clusterDeleteRequest.delete();

        String violationString = "";
        for (ConstraintViolation<PathfinderApplication> violation : violations) {
          violationString += violation.getMessage() + "\n";
        }
        return badRequest(Json.toJson(violationString));
      }
    }
  }

  public Result getApplications() {
    return ok(Json.toJson(PathfinderApplication.find.all()));
  }
}

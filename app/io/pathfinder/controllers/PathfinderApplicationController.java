package io.pathfinder.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.pathfinder.models.PathfinderApplication;

import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class PathfinderApplicationController extends Controller {

  Config config = ConfigFactory.load();

  @Inject
  WSClient ws;

  @BodyParser.Of(BodyParser.Json.class)
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

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();
    Set<ConstraintViolation<PathfinderApplication>> violations = validator.validate(application);

    if (violations.size() == 0) {

      String pathfinderServerURL = config.getString("pathfinder.server.url");
      WSRequest clusterCreateRequest = ws.url(pathfinderServerURL + "/cluster");

      JsonNode clusterNode = Json.newObject();
      Promise<Result> resultPromise = clusterCreateRequest.post(clusterNode).map(
          new Function<WSResponse, Result>() {
            public Result apply(WSResponse response) {
              if (response.getStatus() == Http.Status.CREATED) {
                application.clusterId = response.asJson().findValue("id").longValue();
                application.save();
                return ok(Json.toJson(application));
              } else {
                application.delete();
                return badRequest(response.asJson());
              }
            }
          }
      );

      return resultPromise;
    } else {
      String violationString = "";
      for (ConstraintViolation<PathfinderApplication> violation : violations) {
        violationString += violation.getMessage() + "\n";
      }
      return Promise.pure(badRequest(violationString));
    }
  }

}

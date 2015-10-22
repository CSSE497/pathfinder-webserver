package io.pathfinder.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pathfinder.annotations.interfaces.RequireJson;
import io.pathfinder.models.PathfinderUser;
import io.pathfinder.util.Security;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

public class PathfinderUserController extends Controller{

  @RequireJson()
  public Result createUser() {
    JsonNode jsonNode = request().body().asJson();

    Result result = validateNewUser(jsonNode);
    if(result.status() != Http.Status.OK) {
      return result;
    }

    ObjectNode json = (ObjectNode) jsonNode;
    json.remove("confirmPassword");

    json.put("userToken", Security.generateToken(PathfinderUser.USER_TOKEN_LENGTH));

    try {
      PathfinderUser user = Json.fromJson(json, PathfinderUser.class);
      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();
      Set<ConstraintViolation<PathfinderUser>> violations = validator.validate(user);
      if(violations.size() == 0) {
        user.save();
        return created();
      } else {
        String violationString = "";
        for(ConstraintViolation<?> violation: violations) {
          violationString += violation.getMessage() + "\n";
        }
        return badRequest(violationString);
      }
    } catch (PersistenceException e) {
      return badRequest("Username already exists");
    } catch (RuntimeException e){
      if(e.getCause() instanceof UnrecognizedPropertyException) {
        return badRequest("Unrecognized property in JSON");
      } else {
        throw e;
      }
    }
  }

  @RequireJson()
  public Result getUser() {
    JsonNode jsonNode = request().body().asJson();

    PathfinderUser user = getValidUser(jsonNode);

    if(user != null) {
      return ok(Json.toJson(user));
    }

    return badRequest("Invalid user");
  }

  @RequireJson()
  public Result getUserToken() {
    JsonNode jsonNode = request().body().asJson();

    PathfinderUser user = getValidUser(jsonNode);

    if(user != null) {
      return ok(Json.toJson(user.userToken));
    }

    return badRequest("Invalid user");
  }

  private PathfinderUser getValidUser(JsonNode jsonNode) {
    String username = jsonNode.get("username").asText();
    PathfinderUser user = PathfinderUser.find.byId(username);

    if(user == null) {
      return null;
    }

    String password = jsonNode.get("password").asText();
    if(password.equals(user.password)) {
      return user;
    }

    return null;
  }

  // Will be removed later
  public Result getUsers() {
    List<PathfinderUser> usernames = PathfinderUser.find.all();
    return ok(Json.toJson(usernames));
  }

  private Result validateNewUser(JsonNode jsonNode) {
    if(!jsonNode.isObject()) {
      return badRequest("Json not an object");
    }

    ObjectNode json = (ObjectNode) jsonNode;

    JsonNode jsonUsername = json.get("username");
    if(jsonUsername == null) {
      return badRequest("Username was not provided");
    }

    JsonNode jsonPassword = json.get("password");
    if(jsonPassword == null) {
      return badRequest("Password was not provided.");
    }

    JsonNode jsonConfirmPassword = json.get("confirmPassword");
    if(jsonConfirmPassword == null) {
      return badRequest("Confirm password was not provided");
    }

    String password = jsonPassword.asText();
    String confirmPassword = jsonConfirmPassword.asText();

    if(!password.equals(confirmPassword)) {
      return badRequest("Passwords do not match");
    }

    return ok();
  }
}

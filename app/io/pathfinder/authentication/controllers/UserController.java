package io.pathfinder.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pathfinder.authentication.models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

public class UserController extends Controller{
  public Result createUser() {
    JsonNode jsonNode = request().body().asJson();

    Result result = validateNewUser(jsonNode);
    if(result.status() != 200) {
      return result;
    }

    ObjectNode json = (ObjectNode) jsonNode;
    json.remove("confirmPassword");

    json.put("userToken", generateUserToken());

    User user;
    try {
      user = Json.fromJson(json, User.class);
      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();
      Set<ConstraintViolation<User>> violations = validator.validate(user);
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
      return badRequest("User already exists");
    } catch (RuntimeException e){
      if(e.getCause() instanceof UnrecognizedPropertyException) {
        return badRequest("Unrecognized property in JSON");
      }
      throw e;
    }
  }

  public Result getUser() {
    JsonNode jsonNode = request().body().asJson();

    User user = getValidUser(jsonNode);

    if(user != null) {
      return ok(Json.toJson(user));
    }

    return badRequest("Invalid user");
  }

  public Result getUserToken() {
    JsonNode jsonNode = request().body().asJson();

    User user = getValidUser(jsonNode);

    if(user != null) {
      return ok(Json.toJson(user.userToken));
    }

    return badRequest("Invalid user");
  }

  private User getValidUser(JsonNode jsonNode) {
    String email = jsonNode.get("email").asText();
    User user = User.find.byId(email);

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
    List<User> emails = User.find.all();
    return ok(Json.toJson(emails));
  }

  private Result validateNewUser(JsonNode jsonNode) {
    if(!(jsonNode instanceof ObjectNode)) {
      return badRequest("Json not an object");
    }

    ObjectNode json = (ObjectNode) jsonNode;

    JsonNode jsonEmail = json.get("email");
    if(jsonEmail == null) {
      return badRequest("Email was not provided");
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

  public static String generateUserToken() {
    SecureRandom rand = new SecureRandom();
    byte[] bytes = new byte[User.userTokenLength];

    // This self seeds using the OS's random number generator
    rand.nextBytes(bytes);

    // It must be ASCII, UTF standards create different length Strings for some reason
    return new String(bytes, StandardCharsets.US_ASCII);
  }
}

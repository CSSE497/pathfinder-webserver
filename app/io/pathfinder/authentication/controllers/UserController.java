package io.pathfinder.authentication.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pathfinder.authentication.models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import java.security.SecureRandom;
import java.util.List;

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
      user.save();
      return created();
    } catch (PersistenceException e) {
      return internalServerError("Error saving user to the database: " + e.getMessage());
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

  public Result getUsers() {
    List<Object> emails = User.find.findIds();
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

    String email = jsonEmail.asText();
    User foundUser = User.find.byId(email);
    if(foundUser != null) {
      return badRequest("Email is already in use.");
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

    // note: This self seeds using the OS's random number generator
    rand.nextBytes(bytes);
    return new String(bytes);
  }
}

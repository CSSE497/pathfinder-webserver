package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import annotations.interfaces.RequireJson;
import models.Customer;
import play.data.Form;
import play.libs.Json;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class CustomerController extends Controller {
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";

    public static class Login {
        public String email;
        public String password;
    }

    public Result login() {
        return ok(views.html.login.render(Form.form(Login.class)));
    }

    public Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        return ok();
    }

    public Result register() {
        Logger.info(String.format("Received createUser request: %s", request().body()));
        Map<String,String[]> fields = request().body().asFormUrlEncoded();
        Customer newUser = new Customer();
        newUser.username = fields.get(EMAIL)[0];
        newUser.password = fields.get(PASSWORD)[0];
        newUser.save();
        return created();
    }

    @RequireJson() public Result getUser() {
        JsonNode jsonNode = request().body().asJson();

        Customer user = getValidUser(jsonNode);

        if (user != null) {
            return ok(Json.toJson(user));
        }

        return badRequest("Invalid user");
    }

    @RequireJson() public Result getUserToken() {
        JsonNode jsonNode = request().body().asJson();

        Customer user = getValidUser(jsonNode);

        if (user != null) {
            return ok(Json.toJson(user.userToken));
        }

        return badRequest("Invalid user");
    }

    private Customer getValidUser(JsonNode jsonNode) {
        String username = jsonNode.get("username").textValue();
        Customer user = Customer.find.byId(username);

        if (user == null) {
            return null;
        }

        String password = jsonNode.get("password").textValue();
        if (password.equals(user.password)) {
            return user;
        }

        return null;
    }

    // Will be removed later
    public Result getUsers() {
        List<Customer> usernames = Customer.find.all();
        return ok(Json.toJson(usernames));
    }

    private Result validateNewUser(JsonNode jsonNode) {
        if (!jsonNode.isObject()) {
            return badRequest("Json not an object");
        }

        ObjectNode json = (ObjectNode) jsonNode;

        if (json.size() != Customer.REQUIRED_CREATE_FIELDS) {
            return badRequest("Wrong number of fields to create a user");
        }

        if (!json.has("username") || !json.get("username").isTextual()) {
            return badRequest("Username was not provided");
        }

        if (!json.has("password") || !json.get("password").isTextual()) {
            return badRequest("Password was not provided.");
        }

        if (!json.has("confirmPassword") || !json.get("confirmPassword").isTextual()) {
            return badRequest("Confirm password was not provided");
        }

        String password = json.get("password").textValue();
        String confirmPassword = json.get("confirmPassword").textValue();

        if (!password.equals(confirmPassword)) {
            return badRequest("Passwords do not match");
        }

        return ok();
    }
}

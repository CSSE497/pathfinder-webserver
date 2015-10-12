package io.pathfinder.application.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pathfinder.authentication.models.User;
import io.pathfinder.util.Security;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

public class ApplicationController extends Controller{
  public Result createApplication() {
    JsonNode jsonNode = request().body().asJson();
    
    return ok();
  }
}

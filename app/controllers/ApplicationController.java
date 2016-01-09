package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import annotations.interfaces.RequireJson;
import models.Application;
import util.Security;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class ApplicationController extends Controller {

    Config config = ConfigFactory.load();

    @Inject WSClient ws;

    @RequireJson() public Promise<Result> createApplication() {
        JsonNode jsonNode = request().body().asJson();
        if (!jsonNode.isObject()) {
            return Promise.pure(badRequest("Json sent was not a Json objects"));
        }

        int size = jsonNode.size();
        if (size > Application.REQUIRED_CREATE_FIELDS) {
            return Promise.pure(
                badRequest("Too many fields were detected in the application create request"));
        }

        if (size < Application.REQUIRED_CREATE_FIELDS) {
            return Promise
                .pure(badRequest("Too few fields were detected in the application create request"));
        }

        if (!jsonNode.has("name")) {
            return Promise.pure(badRequest("Json did not include a name field"));
        }

        Application application = Json.fromJson(jsonNode, Application.class);

        String pathfinderServerURL = config.getString("pathfinder.server.url");
        WSRequest clusterCreateRequest = ws.url(pathfinderServerURL + "/cluster");

        JsonNode clusterNode = Json.newObject();
        return clusterCreateRequest.post(clusterNode)
            .map(new CreateApplicationResponse(application));
    }

    public Result getApplications() {
        return ok(Json.toJson(Application.find.all()));
    }


    private class CreateApplicationResponse implements Function<WSResponse, Result> {

        private Application application;

        public CreateApplicationResponse(Application application) {
            this.application = application;
        }

        @Override public Result apply(WSResponse response) throws Throwable {
            if (response.getStatus() != Http.Status.CREATED) {
                return badRequest(response.asJson());
            }

            application.clusterId = response.asJson().findValue("id").longValue();
            application.token = Security.generateToken(Application.TOKEN_LENGTH);

            do {
                application.id = UUID.randomUUID();
            } while (Application.find.byId(application.id) != null);

            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            Validator validator = validatorFactory.getValidator();
            Set<ConstraintViolation<Application>> violations =
                validator.validate(application);

            if (violations.size() == 0) {
                application.save();
                return ok(Json.toJson(application));
            } else {
                String pathfinderServerURL = config.getString("pathfinder.server.url");
                WSRequest clusterDeleteRequest =
                    ws.url(pathfinderServerURL + "/cluster/" + application.clusterId);
                clusterDeleteRequest.delete();

                String violationString = "";
                for (ConstraintViolation<Application> violation : violations) {
                    violationString += violation.getMessage() + "\n";
                }
                return badRequest(Json.toJson(violationString));
            }
        }
    }
}

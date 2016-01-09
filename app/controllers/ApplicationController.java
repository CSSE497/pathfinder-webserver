package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import auth.SignedIn;
import models.Application;
import play.Logger;
import play.data.Form;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.application;

public class ApplicationController extends Controller {
    private static final Config CONFIG = ConfigFactory.load();

    @Inject WSClient ws;

    @Security.Authenticated(SignedIn.class) public Result application(UUID id) {
        Logger.info(String.format("Serving application %s", id));
        Application app =
            Application.find.where().idEq(id).eq("email", session("email")).findUnique();
        List<Application> apps = Application.find.where().eq("email", session("email")).findList();
        session().put("app", id.toString());
        return ok(application.render(app, apps, Form.form(Parameters.class), Form.form(Parameters.class)));
    }

    @Security.Authenticated(SignedIn.class) public Result create() {
        Logger.info("Creating application");
        Form<Create> form = Form.form(Create.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.info(String.format("Application creation failed: %s", form.errors()));
            return badRequest();
        } else {
            Application app = new Application();
            app.name = form.get().name;
            app.email = session("email");
            app.id = UUID.randomUUID();
            app.clusterId = createDefaultCluster();
            app.save();
            Logger.info(String.format("%s created application %s", app.email, app.name));
            return redirect(routes.DashboardController.dashboard());
        }
    }

    @Security.Authenticated(SignedIn.class) public Result setCapacities() {
        Form<Parameters> form = Form.form(Parameters.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.warn(String.format("Setting capacities failed: %s", form.errors()));
            return badRequest("Something went wrong");
        } else {
            System.out.println(form.get().parameters);
            List<String> parameters = form.get().parameters;
            parameters.removeAll(Arrays.asList("", null));
            Logger.info(String.format("Setting capacities: %s", form.get().parameters));
            return redirect(routes.ApplicationController.application(UUID.fromString(session("app"))));
        }
    }

    @Security.Authenticated(SignedIn.class) public Result setObjectives() {
        Form<Parameters> form = Form.form(Parameters.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.warn(String.format("Setting objectives failed: %s", form.errors()));
            return badRequest("Something went wrong");
        } else {
            System.out.println(form.get().parameters);
            List<String> parameters = form.get().parameters;
            parameters.removeAll(Arrays.asList("", null));
            Logger.info(String.format("Setting objectives: %s", form.get().parameters));
            return redirect(routes.ApplicationController.application(UUID.fromString(session("app"))));
        }
    }

    private int createDefaultCluster() {
        return 0;
    }
/*
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

        String pathfinderServerURL = CONFIG.getString("pathfinder.server.url");
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

            do {
                application.id = UUID.randomUUID();
            } while (Application.find.byId(application.id) != null);

            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            Validator validator = validatorFactory.getValidator();
            Set<ConstraintViolation<Application>> violations = validator.validate(application);

            if (violations.size() == 0) {
                application.save();
                return ok(Json.toJson(application));
            } else {
                String pathfinderServerURL = CONFIG.getString("pathfinder.server.url");
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
    */


    public static class Create {
        public String name;
    }


    public static class Parameters {
        public List<String> parameters;
        public String id;
    }
}

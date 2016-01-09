package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import auth.SignedIn;
import models.Application;
import models.CapacityParameter;
import models.Customer;
import models.ObjectiveFunction;
import models.ObjectiveParameter;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.application;

import static java.util.stream.Collectors.toList;
import static play.data.Form.form;

public class ApplicationController extends Controller {
    private static final Config CONFIG = ConfigFactory.load();

    @Inject WSClient ws;

    @Security.Authenticated(SignedIn.class) public Result application(UUID id) {
        Logger.info(String.format("Serving application %s", id));
        Application app = Application.find.byId(id);
        List<Application> apps = Customer.find.byId(session("email")).applications;
        session().put("app", id.toString());
        app.objectiveFunction.refresh();
        return ok(
            application.render(app, apps, form(Parameters.class), form(Parameters.class), form()));
    }

    @Security.Authenticated(SignedIn.class) public Result create() {
        Logger.info("Creating application");
        Form<Create> form = form(Create.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.info(String.format("Application creation failed: %s", form.errors()));
            return badRequest();
        } else {
            Application app = new Application();
            app.name = form.get().name;
            app.customer = Customer.find.byId(session("email"));
            app.id = UUID.randomUUID();
            app.clusterId = createDefaultCluster();
            app.objectiveFunction = ObjectiveFunction.find.byId(ObjectiveFunction.MIN_DIST);
            app.save();
            Logger.info(String.format("%s created application %s", app.customer.email, app.name));
            return redirect(routes.DashboardController.dashboard());
        }
    }

    @Security.Authenticated(SignedIn.class) public Result setCapacities() {
        Form<Parameters> form = form(Parameters.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.warn(String.format("Setting capacities failed: %s", form.errors()));
            return badRequest("Something went wrong");
        } else {
            System.out.println(form.get().parameters);
            List<String> parameters = form.get().parameters;
            parameters.removeAll(Arrays.asList("", null));
            Application app = Application.find.byId(UUID.fromString(session("app")));
            Logger.info(String.format("Setting capacities for %s: %s", app.id, parameters));
            app.refresh();
            app.capacityParameters.forEach(p -> {
                p.delete();
            });
            Collections.reverse(parameters);
            for (String parameterName : parameters) {
                CapacityParameter parameter = new CapacityParameter();
                parameter.application = app;
                parameter.parameter = parameterName;
                parameter.save();
            }
            app.refresh();
            Logger.info(String.format("Capacities for %s: %s", app.id,
                app.capacityParameters.stream().map(x -> x.parameter).collect(toList())));
            return redirect(routes.ApplicationController.application(app.id));
        }
    }

    @Security.Authenticated(SignedIn.class) public Result setObjectives() {
        Form<Parameters> form = form(Parameters.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.warn(String.format("Setting objectives failed: %s", form.errors()));
            return badRequest("Something went wrong");
        } else {
            System.out.println(form.get().parameters);
            List<String> parameters = form.get().parameters;
            parameters.removeAll(Arrays.asList("", null));
            Application app = Application.find.byId(UUID.fromString(session("app")));
            Logger.info(String.format("Setting objectives for %s: %s", app.id, parameters));
            app.objectiveParameters.forEach(p -> p.delete());
            Collections.reverse(parameters);
            for (String parameterName : parameters) {
                ObjectiveParameter parameter = new ObjectiveParameter();
                parameter.application = app;
                parameter.parameter = parameterName;
                parameter.save();
            }
            app.refresh();
            Logger.info(String.format("Objectives for %s: %s", app.id,
                app.objectiveParameters.stream().map(x -> x.parameter).collect(toList())));
            return redirect(routes.ApplicationController.application(app.id));
        }
    }

    @Security.Authenticated(SignedIn.class) public Result setObjectiveFunction() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Application app = Application.find.byId(UUID.fromString(session("app")));
        ObjectiveFunction function;
        switch (requestData.get("functionsradios")) {
            case ObjectiveFunction.MIN_DIST:
                function = ObjectiveFunction.find.byId(ObjectiveFunction.MIN_DIST);
                break;
            case ObjectiveFunction.MIN_TIME:
                function = ObjectiveFunction.find.byId(ObjectiveFunction.MIN_TIME);
                break;
            default:
                function = new ObjectiveFunction();
                function.id = UUID.randomUUID().toString();
                function.function = requestData.get("function");
                function.save();
                break;
        }
        app.objectiveFunction = function;
        app.save();
        Logger.info(String.format("Set objective function for %s: %s", app.id, function.function));
        return redirect(routes.ApplicationController.application(app.id));
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

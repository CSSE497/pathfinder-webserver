package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.application;

import static java.util.stream.Collectors.toList;
import static play.data.Form.form;

public class ApplicationController extends Controller {
    private static final Config CONFIG = ConfigFactory.load();

    @Inject WSClient ws;

    @Security.Authenticated(SignedIn.class) public Result application(String id) {
        Logger.info(String.format("Serving application %s", id));
        Application app = Application.find.byId(id);
        List<Application> apps = Customer.find.byId(session("email")).applications;
        session().put("app", id.toString());
        app.objectiveFunction.refresh();
        return ok(application.render(app, apps, form(), form(), form()));
    }

    @Security.Authenticated(SignedIn.class) public Result create() {
        Logger.info("Creating application");
        DynamicForm form = form().bindFromRequest();
        Application app = new Application();
        app.name = form.get("name");
        app.customer = Customer.find.byId(session("email"));
        app.id = UUID.randomUUID().toString();
        createDefaultCluster(app.id).get(30, TimeUnit.SECONDS);
        app.objectiveFunction = ObjectiveFunction.find.byId(ObjectiveFunction.MIN_DIST);
        app.save();
        Logger.info(String.format("%s created application %s", app.customer.email, app.name));
        return redirect(routes.DashboardController.dashboard());
    }

    @Security.Authenticated(SignedIn.class) public Result setCapacities() {
        DynamicForm form = form().bindFromRequest();
        List<String> parameters = new ArrayList<>();
        for (int i = 0; ; i++) {
            String p = form.field("parameters[" + i + "]").value();
            System.out.println(p);
            if (p == null) break;
            parameters.add(p);
        }
        parameters.removeAll(Arrays.asList("", null));
        Application app = Application.find.byId(session("app"));
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

    @Security.Authenticated(SignedIn.class) public Result setObjectives() {
        DynamicForm form = form().bindFromRequest();
        List<String> parameters = new ArrayList<>();
        for (int i = 0; ; i++) {
            String p = form.field("parameters[" + i + "]").value();
            System.out.println(p);
            if (p == null) break;
            parameters.add(p);
        }
        parameters.removeAll(Arrays.asList("", null));
        Application app = Application.find.byId(session("app"));
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

    @Security.Authenticated(SignedIn.class) @Transactional public Result setObjectiveFunction() {
        DynamicForm requestData = Form.form().bindFromRequest();
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
        SqlUpdate update = Ebean.createSqlUpdate("update application set objective_function_id = :id1 where id = :id2;");
        update.setParameter("id1", function.id);
        update.setParameter("id2", session("app"));
        update.execute();
        /*function.refresh();
        System.out.println(function.id);
        Application app = Application.find.byId(session("app"));
        System.out.println(session("app"));
        System.out.println(app);
        app.objectiveFunction = function;
        app.save();
        app.update();
        Ebean.update(app);
        app.refresh();*/
        Logger.info(String.format("Set objective function for %s: %s", session("app"), function.function));
        return redirect(routes.ApplicationController.application(session("app")));
    }

    private F.Promise<Void> createDefaultCluster(String appId) {
        WSRequest clusterCreateRequest =
            ws.url(CONFIG.getString("pathfinder.server.url") + "/cluster");
        ObjectNode message = Json.newObject();
        message.put("path", appId);
        return clusterCreateRequest.post(message).map(new F.Function<WSResponse, Void>() {
            @Override public Void apply(WSResponse response) {
                return null;
            }
        });
    }
}

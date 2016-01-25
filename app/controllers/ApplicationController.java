package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import auth.SignedIn;
import models.Application;
import models.CapacityParameter;
import models.Customer;
import models.ObjectiveFunction;
import models.ObjectiveParameter;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.application;

import static java.util.stream.Collectors.toList;
import static play.data.Form.form;

@With(ForceHttps.class)
public class ApplicationController extends Controller {
    private static final Config CONFIG = ConfigFactory.load();
    private static final WebSocketClientFactory wsFactory = new WebSocketClientFactory();
    private static final URI socketUri;
    static {
        try {
            wsFactory.start();
            socketUri = new URI(CONFIG.getString("pathfinder.server.socket"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
        try {
            createDefaultCluster(app.id);
        } catch (Exception e) {
            Logger.error("Failed to create default cluster", e);
            e.printStackTrace();
        }
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

    @Security.Authenticated(SignedIn.class) @Transactional public Result setObjectiveFunction()
        throws IOException, InterruptedException, ExecutionException, TimeoutException {
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
                function.dsl = requestData.get("dsl").replaceAll("\r", "");
                function.function = dsl.ObjectiveFunction.compile(function.dsl).getCompiled();
                function.save();
                break;
        }
        forceRouteUpdate(session("app"));
        SqlUpdate update = Ebean.createSqlUpdate("update application set objective_function_id = :id1 where id = :id2;");
        update.setParameter("id1", function.id);
        update.setParameter("id2", session("app"));
        update.execute();
        Logger.info(String.format("Set objective function for %s: %s", session("app"), function.function));
        return redirect(routes.ApplicationController.application(session("app")));
    }

    private void forceRouteUpdate(String appId)
        throws InterruptedException, ExecutionException, TimeoutException, IOException {
        sendMessage(appId, updateDefaultClusterMessage(appId));
    }

    private void createDefaultCluster(String appId)
        throws InterruptedException, ExecutionException, TimeoutException, IOException {
        sendMessage(appId, createDefaultClusterMessage(appId));
    }

    private void sendMessage(String appId, String message)
        throws IOException, InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient client = this.wsFactory.newWebSocketClient();
        client.open(socketUri, new WebSocket.OnTextMessage() {
            @Override public void onOpen(Connection connection) {
                Logger.info(String.format("Sending ws message: %s", message));
                try {
                    connection.sendMessage(message);
                } catch (IOException e) {
                    Logger.error("Failed to create default cluster", e);
                }
            }

            @Override public void onClose(int closeCode, String message) {}
            @Override public void onMessage(String data) {
                Logger.info(String.format("Received ws message: %s", data));
            }
        }).get(60, TimeUnit.SECONDS);
    }

    private String createDefaultClusterMessage(String appId) {
        ObjectNode message = Json.newObject();
        ObjectNode value = Json.newObject();
        value.put("id", appId);
        message.put("message", "Create");
        message.put("model", "Cluster");
        message.set("value", value);
        return message.toString();
    }

    // TODO: This is 100% a hack, but Dan's server has no other way to do this. This will only
    // work for Adam's sample app.
    private String updateDefaultClusterMessage(String appId) {
        ObjectNode message = Json.newObject();
        message.put("message", "Update");
        message.put("model", "Vehicle");
        message.put("id", 1);
        message.set("value", Json.newObject());
        return message.toString();
    }
}

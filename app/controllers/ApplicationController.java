package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.annotation.Transactional;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import auth.SignedIn;
import models.Application;
import models.CapacityParameter;
import models.Customer;
import models.ObjectiveFunction;
import models.ObjectiveParameter;
import models.Permission;
import models.PermissionKey;
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
    private static final URI socketUri;
    static {
        try {
            socketUri = new URI(CONFIG.getString("pathfinder.server.socket"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Security.Authenticated(SignedIn.class)
    public Result setApplication(String id) {
        session().put("app", id);
        return redirect(routes.ApplicationController.application());
    }

    @Security.Authenticated(SignedIn.class)
    public Result application() {
        String id = session().get("app");
        Logger.info(String.format("Serving application %s", id));
        Application app = Application.find.byId(id);
        List<String> whitelist = Permission.find.all().stream()
            .filter(p -> p.key.applicationId.equals(id))
            .map(p -> p.key.email)
            .collect(Collectors.toList());
        app.objectiveFunction.refresh();
        return ok(application.render(app, whitelist, session("id_token"), form(), form(), form()));
    }

    @Security.Authenticated(SignedIn.class)
    public Result create() {
        Logger.info("Creating application");
        DynamicForm form = form().bindFromRequest();
        Application app = new Application();
        app.name = form.get("name");
        app.customer = Customer.find.byId(session("email"));
        app.id = UUID.randomUUID().toString();
        app.objectiveFunction = ObjectiveFunction.find.byId(ObjectiveFunction.MIN_DIST);
        app.authUrl = Application.PATHFINDER_HOSTED_AUTH_URL;
        app.save();
        PermissionKey permissionKey = new PermissionKey();
        permissionKey.applicationId = app.id;
        permissionKey.email = session("email");
        Permission permission = new Permission();
        permission.key = permissionKey;
        permission.permissions = new HashMap<>();
        permission.save();
        try {
            createDefaultCluster(app.id);
        } catch (Exception e) {
            Logger.error("Failed to create default cluster", e);
            e.printStackTrace();
        }
        Logger.info(String.format("%s created application %s", app.customer.email, app.name));
        return redirect(routes.DashboardController.dashboard());
    }

    @Security.Authenticated(SignedIn.class)
    public Result delete(String appId) {
        Logger.info("Deleting application " + appId);
        Application app = Application.find.byId(appId);
        app.delete();
        return ok();
    }

    @Security.Authenticated(SignedIn.class)
    public Result setCapacities() {
        DynamicForm form = form().bindFromRequest();
        List<String> parameters = new ArrayList<>();
        for (int i = 0; ; i++) {
            String p = form.field("parameters[" + i + "]").value();
            if (p == null) break;
            parameters.add(p);
        }
        parameters.removeAll(Arrays.asList("", null));
        Application app = Application.find.byId(session("app"));
        Logger.info(String.format("Setting capacities for %s: %s", app.id, parameters));
        app.refresh();
        app.capacityParameters.forEach(Model::delete);
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
        return redirect(routes.ApplicationController.application());
    }

    @Security.Authenticated(SignedIn.class)
    public Result setObjectives() {
        DynamicForm form = form().bindFromRequest();
        List<String> parameters = new ArrayList<>();
        for (int i = 0; ; i++) {
            String p = form.field("parameters[" + i + "]").value();
            if (p == null) break;
            parameters.add(p);
        }
        parameters.removeAll(Arrays.asList("", null));
        Application app = Application.find.byId(session("app"));
        Logger.info(String.format("Setting objectives for %s: %s", app.id, parameters));
        app.objectiveParameters.forEach(Model::delete);
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
        return redirect(routes.ApplicationController.application());
    }

    @Security.Authenticated(SignedIn.class)
    public Result setAuthProvider() {
        DynamicForm form = form().bindFromRequest();
        String authUrl = form.get("auth_url");
        SqlUpdate update = Ebean.createSqlUpdate("update application set auth_url = :id1 where id = :id2;");
        update.setParameter("id1", authUrl);
        update.setParameter("id2", session("app"));
        update.execute();
        Application app = Application.find.byId(session("app"));
        Logger.info(
            String.format("Processing auth provider form post for %s to %s", app.id, app.authUrl));
        System.out.println(form.data());
        return redirect(routes.ApplicationController.application());
    }

    @Security.Authenticated(SignedIn.class)
    public Result addToWhitelist() {
        DynamicForm form = form().bindFromRequest();
        String newWhitelistedEmail = form.get("email");
        PermissionKey permissionKey = new PermissionKey();
        permissionKey.applicationId = session("app");
        permissionKey.email = newWhitelistedEmail;
        if (Permission.find.byId(permissionKey) == null) {
            Permission permission = new Permission();
            permission.key = permissionKey;
            permission.permissions = new HashMap<>();
            permission.save();
            Logger.info(String.format("Adding %s to whitelist", newWhitelistedEmail));
        }
        return ok();
    }

    @Security.Authenticated(SignedIn.class)
    public Result removeFromWhitelist() {
        DynamicForm form = form().bindFromRequest();
        String email = form.get("email");
        PermissionKey key = new PermissionKey();
        key.applicationId = session("app");
        key.email = email;
        Logger.info(String.format("Revoking permissions for %s", email));
        Permission permission = Permission.find.byId(key);
        if (permission != null) {
            permission.delete();
        } else {
            Logger.warn(String.format(
                "Could not revoke permissions for %s because record was not found", email));
        }
        return ok();
    }

    @Security.Authenticated(SignedIn.class)
    @Transactional
    public Result setObjectiveFunction()
        throws IOException, DeploymentException {
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
        SqlUpdate update = Ebean.createSqlUpdate("update application set objective_function_id = :id1 where id = :id2;");
        update.setParameter("id1", function.id);
        update.setParameter("id2", session("app"));
        update.execute();
        Logger.info(String.format("Set objective function for %s: %s", session("app"), function.function));
        forceRouteUpdate(session("app"));
        return redirect(routes.ApplicationController.application());
    }

    private void forceRouteUpdate(String appId) throws IOException, DeploymentException {
        sendMessage(appId, recalculateMessage("/root"));
    }

    private void createDefaultCluster(String appId) throws IOException, DeploymentException {
        sendMessage(appId, createDefaultClusterMessage(appId));
    }

    private static void sendMessage(String appId, String message) throws IOException, DeploymentException {
        Logger.info(String.format("Attempting to send %s to %s", message, socketUri));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Arrays.asList(appId));
            }
        };
        container.connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                final RemoteEndpoint.Basic remote = session.getBasicRemote();
                session.addMessageHandler(
                    (MessageHandler.Whole<String>) message1 -> Logger.info("Received message from apiserver: " + message1));
                try {
                    remote.sendText(message);
                } catch (IOException e) {
                    Logger.warn("Failed to create default cluster");
                    e.printStackTrace();
                }
            }
        }, ClientEndpointConfig.Builder.create().configurator(configurator).build(), socketUri);
    }

    private static String createDefaultClusterMessage(String appId) {
        ObjectNode message = Json.newObject();
        ObjectNode value = Json.newObject();
        value.put("id", "/root");
        message.put("message", "Create");
        message.put("model", "Cluster");
        message.set("value", value);
        return message.toString();
    }

    private static String recalculateMessage(String clusterId) {
        ObjectNode message = Json.newObject();
        message.put("message", "Recalculate");
        message.put("clusterId", clusterId);
        return message.toString();
    }
}

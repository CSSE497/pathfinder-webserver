package controllers;

import java.util.List;

import auth.SignedIn;
import controllers.ApplicationController.Create;
import models.Application;
import models.Customer;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class DashboardController extends Controller {

    @Security.Authenticated(SignedIn.class) public Result dashboard() {
        Logger.info(String.format("Serving applications for %s", session("email")));
        List<Application> apps = Customer.find.byId(session("email")).applications;
        return ok(views.html.dashboard.render(apps, Form.form(Create.class)));
    }

}

package controllers;

import java.util.List;

import auth.SignedIn;
import models.Application;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.billing;

public class BillingController extends Controller {

    @Security.Authenticated(SignedIn.class) public Result billing() {
        List<Application> apps = Application.find.where().eq("email", session("email")).findList();
        return ok(billing.render(apps));
    }

}

package controllers;

import java.util.List;

import auth.SignedIn;
import models.Application;
import models.Customer;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.billing;

public class BillingController extends Controller {

    @Security.Authenticated(SignedIn.class) public Result billing() {
        List<Application> apps = Customer.find.byId(session("email")).applications;
        return ok(billing.render(apps));
    }

}

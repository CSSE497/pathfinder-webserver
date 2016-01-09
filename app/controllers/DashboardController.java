package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class DashboardController extends Controller {

    @Security.Authenticated(SignedIn.class) public Result dashboard() {
        return ok(views.html.dashboard.render());
    }
}

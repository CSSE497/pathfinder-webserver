package controllers;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

@With(ForceHttps.class)
public class IndexController extends Controller {
    public Result index() {
        boolean loggedin = session("email") != null;
        return ok(views.html.index.render(loggedin, Form.form()));
    }
}

package controllers;

import controllers.CustomerController.Register;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class IndexController extends Controller {
    public Result index() {
        return ok(views.html.index.render(Form.form(Register.class)));
    }
}

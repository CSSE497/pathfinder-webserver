package controllers;

import java.util.List;

import auth.SignedIn;
import models.Application;
import models.Customer;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.index;
import views.html.login;
import views.html.profile;

@With(ForceHttps.class)
public class CustomerController extends Controller {

    public Result login() {
        return ok(views.html.login.render(Form.form()));
    }

    public Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return (redirect(routes.IndexController.index()));
    }

    public Result authenticate() {
        DynamicForm requestData = Form.form().bindFromRequest();
        Logger.warn(requestData.get("email"));
        Logger.warn(requestData.get("password"));
        Logger.warn(requestData.get("butts"));
        String email = requestData.get("email");
        String password = requestData.get("password");
        Login login = new Login(email, password);
        String error = login.validate();
        if (error != null) {
            Logger.info(String.format("User failed to log in: %s", error));
            return redirect(routes.CustomerController.login());
        } else {
            session().clear();
            session("email", email);
            Logger.info(String.format("User %s successfully logged in", email));
            return redirect(routes.DashboardController.dashboard());
        }
    }

    public Result register() {
        DynamicForm requestData = Form.form().bindFromRequest();
        String email = requestData.get("email");
        String password = requestData.get("password");
        String confirm = requestData.get("confirm");
        Register register = new Register(email, password, confirm);
        String error = register.validate();
        if (error != null) {
            Logger.info(String.format("Register failed: %s", error));
            return badRequest(index.render(requestData));
        } else {
            Customer newCustomer = new Customer();
            newCustomer.email = email;
            newCustomer.password = password;
            newCustomer.save();
            session().clear();
            session("email", email);
            Logger.info(String.format("Registered %s", email));
            return redirect(routes.DashboardController.dashboard());
        }
    }

    @Security.Authenticated(SignedIn.class) public Result profile() {
        List<Application> apps = Customer.find.byId(session("email")).applications;
        return ok(profile.render(apps));
    }

    public static class Login {
        private static final String INVALID_ERR = "Invalid email or password";
        public final String email;
        public final String password;

        public Login(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String validate() {
            Logger.info(String.format("Validating login form: %s %s", email, password));
            Customer customer = Customer.find.byId(email);
            if (customer == null || !customer.password.equals(password)) {
                return INVALID_ERR;
            }
            return null;
        }
    }


    public static class Register {
        private static final String MISMATCH_ERR = "Password does not match";
        private static final String DUPLICATE_ERR = "This email address is already registered";
        public final String email;
        public final String password;
        public final String confirm;

        public Register(String email, String password, String confirm) {
            this.email = email;
            this.password = password;
            this.confirm = confirm;
        }

        public String validate() {
            if (Customer.find.byId(email) != null) {
                return DUPLICATE_ERR;
            } else if (!password.equals(confirm)) {
                return MISMATCH_ERR;
            }
            return null;
        }
    }
}

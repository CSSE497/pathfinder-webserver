package controllers;

import models.Customer;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;

public class CustomerController extends Controller {

    public Result login() {
        return ok(views.html.login.render(Form.form(Login.class)));
    }

    public Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return (redirect(routes.IndexController.index()));
    }

    public Result authenticate() {
        Form<Login> form = Form.form(Login.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.info(String.format("User failed to log in: %s", form.errors()));
            return badRequest(login.render(form));
        } else {
            session().clear();
            session("email", form.get().email);
            Logger.info(String.format("User %s successfully logged in", form.get().email));
            return redirect(routes.DashboardController.dashboard());
        }
    }

    public Result register() {
        Form<Register> form = Form.form(Register.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.info(String.format("Register failed %s: %s", form.get().email, form.errors()));
            return badRequest(index.render(form));
        } else {
            Customer newCustomer = new Customer();
            newCustomer.email = form.get().email;
            newCustomer.password = form.get().password;
            newCustomer.save();
            session().clear();
            session("email", form.get().email);
            Logger.info(String.format("Registered %s", form.get().email));
            return ok();
        }
    }

    public static class Login {
        private static final String INVALID_ERR = "Invalid email or password";
        public String email;
        public String password;

        public String validate() {
            Customer customer = Customer.find.byId(email);
            if (customer == null || !customer.getPassword().equals(password)) {
                return INVALID_ERR;
            }
            return null;
        }
    }


    public static class Register {
        private static final String MISMATCH_ERR = "Password does not match";
        private static final String DUPLICATE_ERR = "This email address is already registered";
        public String email;
        public String password;
        public String confirm;

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

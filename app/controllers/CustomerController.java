package controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.util.List;

import auth.SignedIn;
import models.Application;
import models.Customer;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.index;

@With(ForceHttps.class)
public class CustomerController extends Controller {

    public F.Promise<Result> login() {
        DynamicForm form = Form.form().bindFromRequest();
        String idtoken = form.get("idtoken");
        String verifyUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + idtoken;
        return WS.url(verifyUrl).get().map(response -> {
            if (response.getStatus() != 200) {
                return badRequest("Invalid token");
            } else {
                String email = response.asJson().get("email").asText();
                Logger.info("User with email " + email + " is attempting to log in");
                if (Customer.find.byId(email) == null) {
                    Logger.info("Creating new account for " + email);
                    Customer newCustomer = new Customer();
                    newCustomer.email = email;
                    newCustomer.save();
                }
                session("email", email);
                session("id_token", idtoken);
                return ok(email);
            }
        });
    }

    @Security.Authenticated(SignedIn.class)
    public Result updateToken(String idToken) {
        GoogleIdToken.Payload payload = SignedIn.decode(idToken);
        if (payload != null && payload.getEmail() != null) {
            session("id_token", idToken);
            session("email", payload.getEmail());
            Logger.info("Successfully updated id token");
        } else {
            Logger.warn("Failed to update id token");
        }
        return ok();
    }

    public Result logout() {
        session().clear();
        return (redirect(routes.IndexController.index()));
    }
}

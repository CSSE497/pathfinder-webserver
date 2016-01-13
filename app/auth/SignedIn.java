package auth;

import models.Customer;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class SignedIn extends Security.Authenticator {

    @Override public String getUsername(Http.Context ctx) {
        String email = ctx.session().get("email");
        return email != null && Customer.find.byId(email) != null ? email : null;
    }

    @Override public Result onUnauthorized(Http.Context ctx) {
        return redirect("/");
    }
}

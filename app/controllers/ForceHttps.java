package controllers;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class ForceHttps extends Action<Controller> {

    @Override public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        if (!ctx.request().secure()) {
            return F.Promise.promise(
                () -> redirect("https://" + ctx.request().host() + ctx.request().uri()));
        }
        return this.delegate.call(ctx);
    }
}

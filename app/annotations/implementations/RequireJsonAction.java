package annotations.implementations;

import annotations.interfaces.RequireJson;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class RequireJsonAction extends Action<RequireJson> {

    @Override public Promise<Result> call(Http.Context ctx) throws Throwable {
        if (ctx.request().body().asJson() == null) {
            return Promise.pure(badRequest("Invalid content type, JSON is required."));
        }
        return delegate.call(ctx);
    }
}

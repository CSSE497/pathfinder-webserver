package controllers

import play.libs.F
import play.mvc.Action
import play.mvc.Controller
import play.mvc.Http
import play.mvc.Result

public class ForceHttps : Action<Controller>() {
    override fun call(ctx: Http.Context?): F.Promise<Result>? {
        if (ctx != null && !ctx.request().secure()) {
            return F.Promise.promise {
                redirect("https://${ctx.request().host()}${ctx.request().uri()}")
            }
        } else {
            return delegate.call(ctx)
        }

    }
}

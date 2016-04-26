package auth;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import models.Customer;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class SignedIn extends Security.Authenticator {
    private static final String CLIENT_ID = "555706514291-olo3lfltaf92plod7sbefpnn9ci3ks2b.apps.googleusercontent.com";
    private static final String ISSUER = "accounts.google.com";
    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private static final HttpTransport transport;
    static {
        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername(Http.Context ctx) {
        Payload payload = decode(ctx.session().get("id_token"));
        if ((payload != null) && (payload.getEmail() != null) && (Customer.find.byId(payload.getEmail()) != null)) {
            ctx.session().put("email", payload.getEmail());
            return payload.getEmail();
        } else {
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        ctx.session().clear();
        return redirect("/");
    }

    public static Payload decode(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Arrays.asList(CLIENT_ID))
            .setIssuer(ISSUER)
            .build();
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(idTokenString);
            return idToken == null ? null : idToken.getPayload();
        } catch (GeneralSecurityException|IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

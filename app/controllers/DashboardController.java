package controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import auth.SignedIn;
import models.Application;
import models.Customer;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;

@With(ForceHttps.class)
public class DashboardController extends Controller {
    static final KeyPairGenerator keyPairGenerator;
    private static final String PEM_FILE_HEADER = "PATHFINDER APPLICATION PRIVATE KEY";

    static {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(2048);
    }

    @Security.Authenticated(SignedIn.class)
    public Result dashboard() {
        Logger.info(String.format("Serving applications for %s", session("email")));
        List<Application> apps = Customer.find.byId(session("email")).applications;
        Collections.sort(apps, (o1, o2) -> o1.authUrl.compareTo(o2.authUrl));
        return ok(views.html.dashboard.render(apps, Form.form()));
    }

    @Security.Authenticated(SignedIn.class)
    public Result generateKey(String applicationId) throws IOException {
        Logger.info(String.format("Generating new key pair for %s", applicationId));
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        Application app = Application.find.byId(applicationId);
        app.key = keyPair.getPublic().getEncoded();
        app.save();
        String filename = "privatekey-" + applicationId + ".pem";
        String filepath = "generated_keys/" + filename;
        File pemfile = new File(filepath);
        pemfile.getParentFile().mkdirs();
        PemObject pemObject = new PemObject(PEM_FILE_HEADER, keyPair.getPrivate().getEncoded());
        PemWriter writer = new PemWriter(new FileWriter(pemfile));
        writer.writeObject(pemObject);
        writer.flush();
        writer.close();
        response().setContentType("application/x-download");
        response().setHeader("Content-disposition", "attachment; filename=" + filename);
        return ok(pemfile);
    }
}

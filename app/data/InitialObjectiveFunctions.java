package data;

import com.avaje.ebean.Ebean;

import java.io.InputStream;
import java.util.List;

import models.ObjectiveFunction;
import play.Logger;
import play.libs.Yaml;

public class InitialObjectiveFunctions {
    public InitialObjectiveFunctions() {
        if (Ebean.find(ObjectiveFunction.class).findRowCount() == 0) {
            Logger.info("Loading initial objective functions");
            InputStream is =
                this.getClass().getClassLoader().getResourceAsStream("objectivefunctions.yml");
            Object yaml = Yaml.load(is, this.getClass().getClassLoader());
            if (yaml instanceof List) {
                List functions = (List) yaml;
                Ebean.save(functions);
            } else {
                Logger.warn("Failed to parse initial objective functions");
            }
        }
    }
}

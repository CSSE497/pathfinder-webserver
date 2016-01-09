package module;

import com.google.inject.AbstractModule;

import data.InitialObjectiveFunctions;

public class MainModule extends AbstractModule {
    @Override protected void configure() {
        bind(InitialObjectiveFunctions.class).asEagerSingleton();
    }
}

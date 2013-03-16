package au.com.samcday.bincrawl.web;

import au.com.samcday.bincrawl.AppModule;
import au.com.samcday.bincrawl.services.BincrawlServiceManager;
import au.com.samcday.bincrawl.web.auth.HardcodedAuthenticator;
import au.com.samcday.bincrawl.web.entities.Admin;
import au.com.samcday.bincrawl.web.filters.CORSFilter;
import com.fiestacabin.dropwizard.guice.AutoConfigService;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class BincrawlService extends AutoConfigService<BincrawlConfiguration> {
    public static void main(String... args) throws Exception {
        new BincrawlService().run(args);
    }

    @Override
    public void initialize(Bootstrap<BincrawlConfiguration> bincrawlConfigurationBootstrap) {
        bincrawlConfigurationBootstrap.setName("bincrawl");
    }

    @Override
    protected Injector createInjector(BincrawlConfiguration configuration) {
        return Guice.createInjector(new AppModule());
    }

    @Override
    protected void runWithInjector(BincrawlConfiguration configuration, Environment environment, final Injector injector) throws Exception {
        super.runWithInjector(configuration, environment, injector);

        environment.addFilter(CORSFilter.class, "/*");
        environment.addProvider(new BasicAuthProvider<Admin>(new HardcodedAuthenticator(), "bincrawl"));

        ServiceManager sm = injector.getInstance(BincrawlServiceManager.class).get();
        sm.startAsync();
        sm.awaitHealthy();
    }
}

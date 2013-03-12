package au.com.samcday.bincrawl.web;

import au.com.samcday.bincrawl.AppModule;
import com.fiestacabin.dropwizard.guice.AutoConfigService;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
    protected void runWithInjector(BincrawlConfiguration configuration, Environment environment, Injector injector) throws Exception {
        super.runWithInjector(configuration, environment, injector);
    }
}

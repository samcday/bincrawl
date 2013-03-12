package au.com.samcday.bincrawl.services;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class BincrawlServiceManager implements Provider<ServiceManager> {
    private ServiceManager serviceManager;

    @Override
    public ServiceManager get() {
        return this.serviceManager;
    }

    @Inject
    public BincrawlServiceManager(CrawlService crawlService, BinaryProcessService binaryProcessService,
            CompletedBinaryService completedBinaryService) {
        this.serviceManager = new ServiceManager(ImmutableList.<Service>of(
            /*crawlService, */binaryProcessService/*, completedBinaryService*/));
    }
}

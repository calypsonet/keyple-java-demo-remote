package org.cna.keyple.demo.remote.server;

import io.quarkus.runtime.Startup;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManagerFactory;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import javax.ws.rs.Produces;

/**
 * Configure the SAM resource Manager
 *
 */
public class SamResourceManagerConfig {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerConfig.class);

    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private SamResourceManager samResourceManager;

    @Produces
    @Singleton
    public SamResourceManager samResourceManager() {
        logger.info("Init SamResourceManager with PCSC Plugin...");

        // Registers the plugin to the smart card service.
        Plugin plugin = SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(new PluginObservationExceptionHandler() {
            @Override
            public void onPluginObservationError(String pluginName, Throwable e) {
                logger.error("error in reader observer pluginName:{}, error:{}", pluginName, e.getMessage());
            }
        }, new ReaderObservationExceptionHandler() {
            @Override
            public void onReaderObservationError(String pluginName, String readerName, Throwable e) {
                logger.error("error in reader observer pluginName:{}, readerName:{}, error:{}", pluginName, readerName, e.getMessage());
            }
        }));

        if (plugin.getReaders().size() == 0) {
            throw new IllegalStateException(
                    "For the matter of this example, we expect at least one PCSC reader to be connected");
        }

        samResourceManager = SamResourceManagerFactory.instantiate(plugin, samReaderFilter);

        return samResourceManager;
    }

}

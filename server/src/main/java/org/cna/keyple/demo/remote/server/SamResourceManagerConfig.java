package org.cna.keyple.demo.remote.server;

import org.cna.keyple.demo.remote.server.util.PcscReaderUtils;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManagerFactory;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.Produces;

/**
 * Configure the SAM resource Manager
 *
 */
public class SamResourceManagerConfig {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerConfig.class);

    private static final String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    @Produces
    @Singleton
    public SamResourceManager samResourceManager() {
        logger.info("Init SamResourceManager with PCSC Plugin...");

        // Registers the plugin to the smart card service.
        Plugin plugin = SmartCardService.getInstance().registerPlugin(
                new PcscPluginFactory((pluginName, e) -> logger.error("error in reader observer pluginName:{}, error:{}", pluginName, e.getMessage()), (pluginName, readerName, e) -> logger.error("error in reader observer pluginName:{}, readerName:{}, error:{}",
                        pluginName, readerName, e.getMessage())));

        if (plugin.getReaders().size() == 0) {
            throw new IllegalStateException(
                    "For the matter of this example, we expect at least one PCSC reader to be connected");
        }

        PcscReaderUtils.initSamReader(samReaderFilter);

        return SamResourceManagerFactory.instantiate(plugin, samReaderFilter);
    }

}

package org.cna.keyple.demo.distributed.server.controller;

import org.cna.keyple.demo.distributed.server.util.PcscReaderUtils;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManagerFactory;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * This Singleton configure the SAM reader and the SAM resource Manager
 */
@Singleton
public class SamResourceService {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceService.class);

    public static String SAM_READER_FILTER = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    //sam resource manager handles ressource allocation
    SamResourceManager samResourceManager;

    //Plugin to use for the SAM
    Plugin plugin;

    /**
     * Public constructor invoked at server boot.
     * Initialize the Sam Reader and the Sam Resource Manager.
     * @throws KeypleReaderNotFoundException is no Sam Pcsc reader is found
     */
    public SamResourceService(){
        plugin = initSamPlugin();
        PcscReaderUtils.initSamReader(SAM_READER_FILTER);
        samResourceManager = SamResourceManagerFactory.instantiate(plugin, SAM_READER_FILTER);
    }

    /**
     * Return the Sam Resource Manager
     * @return a not nullable instance of the Sam Resource Manager
     */
    public SamResourceManager getSamResourceManager() {
        return samResourceManager;
    }

    /**
     * Return the Sam Reader
     * @return a not nullable instance of a reader
     * @throws KeypleReaderNotFoundException is no Sam Pcsc reader is found
     */
    public Reader getSamReader(){
        return PcscReaderUtils.getReaderByPattern(SAM_READER_FILTER);
    }

    private Plugin initSamPlugin(){
        SmartCardService smartCardService = SmartCardService.getInstance();

        //return plugin is already register
        if(smartCardService.isRegistered("PcscPlugin")){
            return smartCardService.getPlugin("PcscPlugin");
        }

        // Registers the plugin to the smart card service.
        Plugin plugin = smartCardService.registerPlugin(
                new PcscPluginFactory(new PluginObservationExceptionHandler() {
                    @Override
                    public void onPluginObservationError(String pluginName, Throwable e) {
                        logger.error("error in reader observer pluginName:{}, error:{}", pluginName, e.getMessage());
                    }
                }, new ReaderObservationExceptionHandler() {
                    @Override
                    public void onReaderObservationError(String pluginName, String readerName, Throwable e) {
                        logger.error("error in reader observer pluginName:{}, readerName:{}, error:{}",
                                pluginName, readerName, e.getMessage());
                    }
                }));

        if (plugin.getReaders().size() == 0) {
            throw new IllegalStateException(
                    "For the matter of this example, we expect at least one PCSC reader to be connected");
        }
        return plugin;
    }

}

package org.cna.keyple.demo.remote.server;

import org.cna.keyple.demo.remote.server.util.PcscReaderUtils;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManagerFactory;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Configure the SAM resource Manager
 *
 */
@Singleton
public class SamResourceService {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceService.class);

    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    SamResourceManager samResourceManager;
    Plugin plugin;

    public SamResourceService(){
        plugin = samPlugin();
        PcscReaderUtils.initSamReader(samReaderFilter);
        samResourceManager = SamResourceManagerFactory.instantiate(plugin, samReaderFilter);
    }

    private Plugin samPlugin(){

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


    public SamResourceManager getSamResourceManager() {
        return samResourceManager;
    }

    public Reader getSamReader(){
        return PcscReaderUtils.getReaderByPattern(samReaderFilter);
    }
}

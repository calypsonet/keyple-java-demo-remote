package org.cna.keyple.demo.distributed.server.plugin;


import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.ConfigurationUtil;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactoryBuilder;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

import static org.cna.keyple.demo.distributed.server.util.ConfigurationUtil.setupCardResourceService;

/**
 * This Singleton configure the SAM reader and the SAM resource Manager
 */
@ApplicationScoped
public class SamResourceConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceConfiguration.class);

    //filter to define which reader is used for SAM
    @ConfigProperty(name = "sam.pcsc.reader.filter")
    String samReaderFilter;

    //Plugin to use for the SAM
    Plugin plugin;

    public SamResourceConfiguration(){
        this.samReaderFilter =ConfigProvider.getConfig().getValue("sam.pcsc.reader.filter", String.class);
        logger.info("Init SamCardConfiguration with filter : {}", samReaderFilter);
        //initSam();
    }
    /**
     * Public constructor invoked at server boot.
     * Initialize the Sam Reader and the Sam Resource Manager.
     */
    public SamResourceConfiguration(String samReaderFilter){
        this.samReaderFilter = samReaderFilter;
        logger.info("Init SamCardConfiguration with filter : {}", samReaderFilter);
    }

    public void init(){
        plugin = initSamPlugin();
        //PcscReaderUtils.initSamReader(samReaderFilter);
        setupCardResourceService(
                plugin, samReaderFilter, CalypsoConstants.SAM_PROFILE_NAME);
    }

    /**
     * Return the Sam Reader
     * @return a not nullable instance of a reader
     */
    public Reader getSamReader(){
        return ConfigurationUtil.getReaderByPattern(samReaderFilter);
    }

    private Plugin initSamPlugin(){
        // Get the instance of the SmartCardService (singleton pattern)
        SmartCardService smartCardService = SmartCardServiceProvider.getService();

        //return plugin is already register
        if(smartCardService.getPlugin("PcscPlugin")!=null){
            return smartCardService.getPlugin("PcscPlugin");
        }

        // Register the PcscPlugin with the SmartCardService, do not specify any regex for the type
        // identification (see use case 1), get the corresponding generic plugin in return.
        Plugin plugin = smartCardService.registerPlugin(
                PcscPluginFactoryBuilder.builder().build());

        if (plugin.getReaders().size() == 0) {
            throw new IllegalStateException(
                    "For the matter of this example, we expect at least one PCSC reader to be connected");
        }
        return plugin;
    }

    public String getSamReaderFilter() {
        return samReaderFilter;
    }

}

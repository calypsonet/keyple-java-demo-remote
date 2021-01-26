package org.cna.keyple.demo.remote.server;

import org.cna.keyple.demo.remote.server.util.CalypsoUtils;
import org.cna.keyple.demo.remote.server.util.PcscReaderUtils;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;

/**
 * Configure the SAM resource Manager
 *
 */
public class SamResourceManagerConfig {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerConfig.class);

    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    /**
     * Operate the SAM selection
     *
     * @return a CalypsoSam object if the selection succeed
     * @throws IllegalStateException if the selection fails
     */
    @RequestScoped
    CardResource<CalypsoSam> selectSam() {

        SmartCardService.getInstance().registerPlugin(
                new PcscPluginFactory(null,null));

        Reader samReader =  PcscReaderUtils.initSamReader(samReaderFilter);

        logger.info("SAM Reader configured : {}", samReader.getName());

        CalypsoSam calypsoSam = CalypsoUtils.selectSam(samReader);

        return new CardResource<>(samReader, calypsoSam);
    }

}

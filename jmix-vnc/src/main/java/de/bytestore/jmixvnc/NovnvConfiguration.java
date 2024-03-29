package de.bytestore.jmixvnc;

import de.bytestore.jmixvnc.handler.VNCHandler;
import io.jmix.core.DataManager;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {EclipselinkConfiguration.class, FlowuiConfiguration.class})
@PropertySource(name = "de.bytestore.jmixvnc", value = "classpath:/de/bytestore/jmixvnc/module.properties")
public class NovnvConfiguration {
    private final DataManager dataManager;

    public NovnvConfiguration(DataManager dataManager) {
        this.dataManager = dataManager;

        // Set DataManager for Static Usage (Don't know how to use without [;-(] )
        VNCHandler.setDataManager(dataManager);
    }

    @Bean("novnv_NovnvViewControllers")
    public ViewControllersConfiguration screens(final ApplicationContext applicationContext,
                                                final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("de.bytestore.jmixvnc"));
        return viewControllers;
    }

    @Bean("novnv_NovnvActions")
    public ActionsConfiguration actions(final ApplicationContext applicationContext,
                                        final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ActionsConfiguration actions
                = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actions.setBasePackages(Collections.singletonList("de.bytestore.jmixvnc"));
        return actions;
    }
}

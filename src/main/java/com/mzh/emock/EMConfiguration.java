package com.mzh.emock;

import com.mzh.emock.log.Logger;
import com.mzh.emock.processor.EMApplicationReadyProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = EMConfigurationProperties.Constant.CONFIGURATION_PREFIX,
        name = EMConfigurationProperties.Constant.ENABLED_CONFIGURATION_NAME,
        havingValue = EMConfigurationProperties.Constant.ENABLED_CONFIGURATION_VALUE)
@EnableConfigurationProperties(EMConfigurationProperties.class)
public class EMConfiguration {
    Logger logger=Logger.get(EMConfiguration.class);
    public EMConfiguration(){
        logger.info("EMConfiguration loaded");
    }


    @Bean
    @Conditional(EMConfigurationProperties.ProcessorMatcher.class)
    @DependsOn(EMConfigurationProperties.Constant.PROPERTIES_FILE_NAME)
    public EMApplicationReadyProcessor emApplicationReadyProcessor(@Autowired ApplicationContext context,@Autowired ResourceLoader resourceLoader){
        return new EMApplicationReadyProcessor(context,resourceLoader);
    }
}

package com.mzh.emock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = EMockConfigurationProperties.PREFIX,
        name = EMockConfigurationProperties.NAME,
        havingValue = EMockConfigurationProperties.ENABLED)
@EnableConfigurationProperties(EMockConfigurationProperties.class)
public class EMockConfiguration {

}

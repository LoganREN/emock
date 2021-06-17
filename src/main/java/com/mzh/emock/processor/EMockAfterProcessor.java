package com.mzh.emock.processor;

import com.mzh.emock.EMockConfiguration;
import com.mzh.emock.EMockConfigurationProperties;
import com.mzh.emock.core.EMockSupport;
import com.mzh.emock.log.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@ConditionalOnBean(EMockConfiguration.class)
@Conditional(EMockConfigurationProperties.ProcessorMatcher.class)
public class EMockAfterProcessor implements ApplicationListener<ApplicationReadyEvent>, Ordered {
    private static final LogSupport logger=LogSupport.getLogger(EMockAfterProcessor.class);

    private final ApplicationContext context;
    private final ResourceLoader resourceLoader;

    public EMockAfterProcessor(@Autowired ApplicationContext context, @Autowired ResourceLoader resourceLoader){
        this.context=context;
        this.resourceLoader=resourceLoader;
        logger.info("init PMockProcessor: bean:defaultProcessor,context:"+context.toString()+",resourceLoader:"+resourceLoader.toString());
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Thread main=Thread.currentThread();
        new Thread(()->{
            LocalDateTime startTime= LocalDateTime.now();
            long maxWaiting= EMockConfigurationProperties.WAIT_FOR_APPLICATION_READY;
            try{
                main.join(maxWaiting);
            }catch (Exception ex){
                logger.error("wait for main thread complete error",ex);
            }
            long waitTimes=startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS);
            if(waitTimes>=maxWaiting){
                logger.info("wait for main thread complete until "+maxWaiting+" stop mock initial");
                return;
            }
            initialMockBeans();
        }).start();
    }
    private void initialMockBeans() {
        try {
            logger.info("begin to init mock bean");
            EMockSupport.loadMatchedBeanDefinition(context, resourceLoader);
            EMockSupport.createMockBeanDefinition(context);
            EMockSupport.createMockBeanIfNecessary(context);
            EMockSupport.afterCreateMockBean();
            EMockSupport.proxyAndInject(context);
            logger.info("mock bean completed");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

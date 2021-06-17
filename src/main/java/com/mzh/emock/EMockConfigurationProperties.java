package com.mzh.emock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "com.mzh")
public class EMockConfigurationProperties {
    public static final String PREFIX="com.mzh";
    public static final String NAME="e-mock";
    public static final String ENABLED="enabled";
    public static long WAIT_FOR_APPLICATION_READY=5*60*1000L;
    public static String ENABLED_PROCESSOR="";
    public static final  List<String> ENABLED_PROFILES= Collections.synchronizedList(new ArrayList<String>(){{add("test");add("dev");}});
    public static final List<String> SCAN_PATH=Collections.synchronizedList(new ArrayList<>());



    public void setEnabledProfiles(@NonNull List<String> profiles){
        ENABLED_PROFILES.clear();
        ENABLED_PROFILES.addAll(profiles);
    }

    public void setScanPath(@NonNull List<String> paths){
        SCAN_PATH.clear();
        SCAN_PATH.addAll(paths);
    }

    public void setWaitTime(long waitTime){
        if(waitTime<30*1000L){
            WAIT_FOR_APPLICATION_READY=30*1000L;
            return;
        }
        WAIT_FOR_APPLICATION_READY=waitTime;
    }
    public void setEnabledProcessor(@NonNull String processorName){
        ENABLED_PROCESSOR=processorName;
    }

    public class ProcessorMatcher implements Condition{
        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            if(annotatedTypeMetadata instanceof ClassMetadata){
                return ((ClassMetadata)annotatedTypeMetadata).getClassName().equals(ENABLED_PROCESSOR);
            }
            return false;
        }
    }

}

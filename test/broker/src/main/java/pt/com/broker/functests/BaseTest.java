package pt.com.broker.functests;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetProtocolType;

import java.util.Arrays;
import java.util.Collection;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 08-07-2014.
 */
public class BaseTest {
	
	static final Logger log = LoggerFactory.getLogger(BaseTest.class);
	
	public static final String AGENT_LAUNCH_SYSTEM_PROPERTY 		= "agent-launch";

    protected static boolean runOnce = true;
    protected static boolean lauchAgent = false;

    @BeforeClass()
    public static void loadConfig(){
    	loadSystemProperties();
        if(runOnce) {
            ConfigurationInfo.init();
            Test.setDefaultimeout(10000);
            runOnce = false;
        }
        brokerAgentsLaucher();
    }

    private static final void loadSystemProperties(){
    	log.info("Loading system properties.");
    	try{
    		lauchAgent = Boolean.valueOf(System.getProperty(AGENT_LAUNCH_SYSTEM_PROPERTY));
    		log.debug("Launch agent system property: {}", lauchAgent);
    	} catch(Exception e){
    		log.error("Error loading system properties.", e);
    	}
    }
    
    private static final void brokerAgentsLaucher(){
    	if(lauchAgent){
    		pt.com.broker.Start.start();
    	}
    }

}

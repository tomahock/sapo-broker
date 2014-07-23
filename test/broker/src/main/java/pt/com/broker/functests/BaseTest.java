package pt.com.broker.functests;

import org.junit.BeforeClass;
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

    protected static boolean runOnce = true;

    @BeforeClass()
    public static void loadConfig(){

        if(runOnce) {
            ConfigurationInfo.init();


            Test.setDefaultimeout(20000);
            runOnce = false;
        }
    }


}

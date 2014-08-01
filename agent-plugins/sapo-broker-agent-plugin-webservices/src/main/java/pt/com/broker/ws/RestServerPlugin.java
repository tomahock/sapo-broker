package pt.com.broker.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.core.AgentPlugin;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * @see LICENSE.TXT
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 01-08-2014.
 */
public class RestServerPlugin implements AgentPlugin {

    private static final Logger log = LoggerFactory.getLogger(RestServerPlugin.class);

    @Override
    public void start(Gcs gcs) {

        int wsPort = GcsInfo.getBrokerWsPort();


        if(wsPort>0){

            RestServer restServer = new RestServer();

            try {
                restServer.start(wsPort);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
            log.warn("Not starting Broker WebService");
        }

    }
}

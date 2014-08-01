package pt.com.broker;

import pt.com.broker.core.AgentPlugin;
import pt.com.broker.core.FilePublisher;
import pt.com.gcs.messaging.Gcs;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * @see LICENSE.TXT
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 01-08-2014.
 */
public class FilePublisherService implements AgentPlugin {

    @Override
    public void start(Gcs gcs) {
        FilePublisher.init();
    }
}

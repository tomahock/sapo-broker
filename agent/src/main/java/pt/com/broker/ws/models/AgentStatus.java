package pt.com.broker.ws.models;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * @see LICENSE.TXT
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 26-06-2014.
 */
public class AgentStatus {

    String version;

    String name;

    long systemMessageFailures;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getSystemMessageFailures() {
        return systemMessageFailures;
    }

    public void setSystemMessageFailures(long systemMessageFailures) {
        this.systemMessageFailures = systemMessageFailures;
    }
}

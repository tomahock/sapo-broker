package pt.com.broker.ws.rest;

import io.netty.channel.Channel;

import org.caudexorigo.*;

import pt.com.broker.types.stats.MiscStats;
import pt.com.broker.ws.models.Agent;
import pt.com.broker.ws.models.AgentStatus;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
@Path("/agents")
public class Agents {

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public List<Agent> getOpenQueues() {

        return  getData();
    }


    @GET()
    @Path("/self")
    @Produces(MediaType.APPLICATION_JSON)
    public AgentStatus getStatus() {
    	String version  = System.getProperty("project-version");
    	AgentStatus status = new AgentStatus();
        status.setName(GcsInfo.getAgentName());
        //FIXME: Change the next line
        status.setVersion(version != null ? version : "");
        status.setSystemMessageFailures(MiscStats.getSystemMessagesFailures());

        return status;
    }


    @POST()
    @Path("/self/shutdown")
    @Produces(MediaType.APPLICATION_JSON)
    public void shutdown() {
    	//TODO: Fix this method

//        Runnable kill = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                Shutdown.now();
//            }
//        };
//
//        BrokerExecutor.schedule(kill, 1000, TimeUnit.MILLISECONDS);

    }



    public List<Agent> getData(){

        List<Agent> list = new ArrayList<>();

        for(Channel channel : Gcs.getManagedConnectorSessions()){
            list.add(new Agent(channel));
        }


        return list;
    }

}

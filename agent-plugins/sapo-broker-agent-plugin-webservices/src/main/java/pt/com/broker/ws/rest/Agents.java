package pt.com.broker.ws.rest;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import pt.com.broker.types.stats.MiscStats;
import pt.com.broker.ws.models.Agent;
import pt.com.broker.ws.models.AgentStatus;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
@Path("/agents")
@Api(value = "/agents", description = "Operations about agents")
@Produces({ "application/json" })
public class Agents
{

	@GET()
	@Path("/self/queues")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Agent queue status information.", notes = "Returns the agent current queue status.", response = AgentStatus.class)
	public List<Agent> getOpenQueues()
	{
		return getData();
	}

	@GET()
	@Path("/self")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Agent status information.", notes = "Returns the agent version, name and system failures.", response = AgentStatus.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "The agent status information.")
	})
	public AgentStatus getStatus()
	{
		String version = System.getProperty("project-version");
		AgentStatus status = new AgentStatus();
		status.setName(GcsInfo.getAgentName());
		// FIXME: Change the next line
		status.setVersion(version != null ? version : "");
		status.setSystemMessageFailures(MiscStats.getSystemMessagesFailures());

		return status;
	}

	// TODO: Missing the command return status.
	@POST()
	@Path("/self/shutdown")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Shutsdown the agent.", notes = "Immediately issues a shutdown command to the agent. The agent will shutdown as soon as all current taks finishes.")
	public void shutdown()
	{
		// TODO: Fix this method

		// Runnable kill = new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// Shutdown.now();
		// }
		// };
		//
		// BrokerExecutor.schedule(kill, 1000, TimeUnit.MILLISECONDS);

	}

	public List<Agent> getData()
	{

		List<Agent> list = new ArrayList<>();

		for (Channel channel : Gcs.getManagedConnectorSessions())
		{
			list.add(new Agent(channel));
		}

		return list;
	}

}

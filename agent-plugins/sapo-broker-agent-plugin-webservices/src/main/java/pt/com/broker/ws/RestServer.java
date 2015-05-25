package pt.com.broker.ws;

//import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.ws.filter.CORSResponseFilter;
import pt.com.broker.ws.models.Error;
import pt.com.broker.ws.providers.NotFoundMapper;
import pt.com.broker.ws.swagger.BrokerSwaggerUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * 
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class RestServer
{

	private static final Logger log = LoggerFactory.getLogger(RestServer.class);

	ObjectMapper mapper = new ObjectMapper();

	public void start(int port) throws Exception
	{
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(
				"com.wordnik.swagger.jaxrs.json",
				"pt.com.broker.ws.rest",
				"pt.com.broker.ws.swagger"
				);
		resourceConfig.register(JacksonFeature.class);
		resourceConfig.register(NotFoundMapper.class);
		resourceConfig.register(com.wordnik.swagger.jersey.listing.ApiListingResourceJSON.class);
		resourceConfig.register(com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider.class);
		resourceConfig.register(com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider.class);
		resourceConfig.register(CORSResponseFilter.class);

		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);

		Server server = new Server(port);

		for (Connector y : server.getConnectors())
		{
			for (ConnectionFactory x : y.getConnectionFactories())
			{
				if (x instanceof HttpConnectionFactory)
				{
					((HttpConnectionFactory) x).getHttpConfiguration().setSendServerVersion(false);
				}
			}
		}

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/broker");
		context.addServlet(sh, "/*");

		context.setErrorHandler(new ErrorHandler()
		{

			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException
			{

				response.setContentType("application/json");
				response.setStatus(500);

				mapper.writeValue(response.getOutputStream(), Error.INVALID_REQUEST);
			}

		});

		ContextHandler resourceContext = new ContextHandler("/backoffice/");

		ResourceHandler resourceHandler = new ResourceHandler();

		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase("etc/assets/");

		resourceContext.setHandler(resourceHandler);
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { context, resourceContext });

		server.setHandler(handlers);
		BrokerSwaggerUtil.getBeanConfig();
		BrokerSwaggerUtil.getApiInfo();

		server.start();

	}

}

package pt.com.broker.ws;


import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import pt.com.broker.ws.models.Error;
import pt.com.broker.ws.providers.NotFoundMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *

 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class RestServer {

    ObjectMapper mapper = new ObjectMapper();


    public void start(int port) throws Exception{
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages("pt.com.broker.ws.rest");
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(NotFoundMapper.class);


        ServletContainer servletContainer = new ServletContainer(resourceConfig);
        ServletHolder sh = new ServletHolder(servletContainer);


        Server server = new Server(port);

        for(Connector y : server.getConnectors()) {
            for(ConnectionFactory x  : y.getConnectionFactories()) {
                if(x instanceof HttpConnectionFactory) {
                    ((HttpConnectionFactory)x).getHttpConfiguration().setSendServerVersion(false);
                }
            }
        }




        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/broker");
        context.addServlet(sh, "/*");
        context.setErrorHandler(new ErrorHandler() {


            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {


                response.setContentType("application/json");
                response.setStatus(500);

                mapper.writeValue(response.getOutputStream(),Error.INVALID_REQUEST);
            }


        });

        ContextHandler resourceContext = new ContextHandler("/backoffice/");

        ResourceHandler resource_handler = new ResourceHandler();

        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("etc/assets/");

        resourceContext.setHandler(resource_handler);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context, resourceContext});


        server.setHandler(handlers);


        server.start();

    }

}

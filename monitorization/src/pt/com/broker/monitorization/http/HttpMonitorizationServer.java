/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package pt.com.broker.monitorization.http;

import org.caudexorigo.http.netty.NettyHttpServer;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collectors.CollectorManager;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;
import pt.com.broker.monitorization.consolidator.db.H2ConsolidatorManager;

public class HttpMonitorizationServer {
	
	private static final Logger log = LoggerFactory.getLogger(HttpMonitorizationServer.class);
	
    public static void main(String[] args)
    {
    	InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		
    	log.info("Starting Sapo-Broker HTTP Monitorization Server...");

    	ConfigurationInfo.init();
    	CollectorManager.init();
		H2ConsolidatorManager.init();
		
		int port = ConfigurationInfo.getConsoleHttpPort();
		String host = "0.0.0.0";

		NettyHttpServer server = new NettyHttpServer("./wwwroot/");
		server.setPort(port);
		server.setHost(host);
		
		server.setRouter(new ActionRouter());
		
		server.start();
		log.info("Monitorization Console is accessible at 'http://localhost:{}/main.html'", port+"");
    }
}

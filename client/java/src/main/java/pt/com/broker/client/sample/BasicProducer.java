/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.com.broker.client.sample;

import java.io.InputStream;

import org.caudexorigo.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetBrokerMessage;

/**
 *
 * @author brsantos
 */
public class BasicProducer
{

	private static final Logger log = LoggerFactory.getLogger(BasicProducer.class);

	public static void main(String[] args)
	{
		try
		{
			InputStream stream = BasicConsumer.class.getResourceAsStream("/payload-test.xml");
			BrokerClient client = new BrokerClient("127.0.0.1", 3323);
			client.publishMessage(new NetBrokerMessage(IOUtils.toByteArray(stream)), "/dev/sapo/promos/new/Continente");
		}
		catch (Throwable ex)
		{
			log.error(ex.getMessage(), ex);
		}
	}
}

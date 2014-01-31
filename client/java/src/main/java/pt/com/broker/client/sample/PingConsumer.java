package pt.com.broker.client.sample;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetPong;

public class PingConsumer
{

	public static void main(String[] args) throws Throwable
	{
		BrokerClient bk = new BrokerClient("broker.bk.sapo.pt", 3323);

		NetPong pong = bk.checkStatus();

		System.out.println(pong);
		System.out.println(pong.getActionId());

	}
}
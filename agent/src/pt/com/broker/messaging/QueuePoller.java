package pt.com.broker.messaging;

import org.apache.mina.core.session.IoSession;

import pt.com.broker.types.NetPoll;

public class QueuePoller implements Runnable
{
	private final NetPoll _poll;
	private final IoSession _iosession;

	public QueuePoller(NetPoll poll, IoSession iosession)
	{
		_poll = poll;
		_iosession = iosession;
	}

	@Override
	public void run()
	{
		BrokerSyncConsumer.poll(_poll, _iosession);
	}

}

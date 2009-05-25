package pt.com.broker.client.utils;

import pt.com.broker.client.messaging.MessageAcceptedListener;
import pt.com.broker.types.NetFault;

public class BlockingMessageAcceptedListener implements MessageAcceptedListener
{
	private final Object syncObj;
	private boolean timeout = false;
	private NetFault fault = null;
	
	public BlockingMessageAcceptedListener(Object syncObj)
	{
		this.syncObj = syncObj;
	}
	
	@Override
	public void messageAccepted(String actionId)
	{
		synchronized (syncObj)
		{
			syncObj.notifyAll();
		}			
	}

	@Override
	public void messageTimedout(String actionId)
	{
		synchronized (syncObj)
		{
			timeout = true;
			syncObj.notifyAll();
		}
	}

	@Override
	public void messageFailed(NetFault fault)
	{
		synchronized (syncObj)
		{
			this.fault = fault;
			syncObj.notifyAll();
		}		
	}
	
	public boolean wasTimeout()
	{
		return timeout;
	}
	
	public boolean wasFailure()
	{
		return fault != null;
	}
	
	public NetFault getFault()
	{
		return fault;
	}
}

package pt.com.broker.functests.helpers;

import pt.com.broker.functests.Test;
import pt.com.types.NetProtocolType;

public abstract class BrokerTest extends Test
{

	private static NetProtocolType defaultEncodingProtocolType = NetProtocolType.PROTOCOL_BUFFER;

	private NetProtocolType encodingProtocolType;
	
	private boolean constructionFailed = false;
	private Throwable reasonForFailure;

	public BrokerTest(String testName)
	{
		super(testName);
		encodingProtocolType = getDefaultEncodingProtocolType();
	}

	public void setFailure(Throwable throwable)
	{
		setConstructionFailed(true);
		setReasonForFailure(throwable);

	}
	
	public void setEncodingProtocolType(NetProtocolType encodingProtocolType)
	{
		this.encodingProtocolType = encodingProtocolType;
	}

	public NetProtocolType getEncodingProtocolType()
	{
		return encodingProtocolType;
	}

	public static void setDefaultEncodingProtocolType(NetProtocolType defaultEncodingProtocolType)
	{
		synchronized (defaultEncodingProtocolType)
		{
			BrokerTest.defaultEncodingProtocolType = defaultEncodingProtocolType;
		}
	}

	public static NetProtocolType getDefaultEncodingProtocolType()
	{
		synchronized (defaultEncodingProtocolType)
		{
			return defaultEncodingProtocolType;
		}
	}

	public void setConstructionFailed(boolean constructionFailed)
	{
		this.constructionFailed = constructionFailed;
	}

	public boolean isConstructionFailed()
	{
		return constructionFailed;
	}

	public void setReasonForFailure(Throwable reasonForFailure)
	{
		this.reasonForFailure = reasonForFailure;
	}

	public Throwable getReasonForFailure()
	{
		return reasonForFailure;
	}
}

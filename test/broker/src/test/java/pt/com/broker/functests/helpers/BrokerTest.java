package pt.com.broker.functests.helpers;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.com.broker.functests.Test;
import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetProtocolType;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public abstract class BrokerTest extends Test
{
	private static NetProtocolType defaultEncodingProtocolType = NetProtocolType.PROTOCOL_BUFFER;

	private NetProtocolType encodingProtocolType;

	private boolean constructionFailed = false;
	private Throwable reasonForFailure;

	private static int defaultDataLenght = 200;

	protected int dataLenght = getDefaultDataLenght();

	protected byte[] rawData = null;

	public BrokerTest(NetProtocolType protocolType)
	{
		super("");
		encodingProtocolType = protocolType;
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

	public void setData(byte[] data)
	{
		synchronized (this)
		{
			this.rawData = data;
		}
	}

	public byte[] getData()
	{
		synchronized (this)
		{
			if (rawData == null)
				rawData = RandomStringUtils.randomAlphanumeric(dataLenght).getBytes();
			return rawData;
		}
	}

	public static void setDefaultDataLenght(int defaultDataLenght)
	{
		synchronized (BrokerTest.class)
		{
			BrokerTest.defaultDataLenght = defaultDataLenght;
		}

	}

	public static int getDefaultDataLenght()
	{
		synchronized (BrokerTest.class)
		{
			return defaultDataLenght;
		}
	}


    public int getAgent1SSLPort()
    {
        return  Integer.parseInt(ConfigurationInfo.getParameter("agent1-legacy-port"));
    }

	public static int getAgent1Port()
	{
		NetProtocolType defaultEncodingProtocolType = BrokerTest.getDefaultEncodingProtocolType();
		int port = 0;
		if (defaultEncodingProtocolType.equals(NetProtocolType.SOAP_v0))
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent1-legacy-port"));
		}
		else
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent1-port"));
		}
		return port;
	}

	public static int getAgent2Port()
	{
		NetProtocolType defaultEncodingProtocolType = BrokerTest.getDefaultEncodingProtocolType();
		int port = 0;
		if (defaultEncodingProtocolType.equals(NetProtocolType.SOAP_v0))
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent2-legacy-port"));
		}
		else
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent2-port"));
		}
		return port;
	}

	public static int getAgent1UdpPort()
	{
		NetProtocolType defaultEncodingProtocolType = BrokerTest.getDefaultEncodingProtocolType();
		int port = 0;
		if (defaultEncodingProtocolType.equals(NetProtocolType.SOAP_v0))
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent1-legacy-udp-port"));
		}
		else
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent1-udp-port"));
		}
		return port;
	}

	public static int getAgent2UdpPort()
	{
		NetProtocolType defaultEncodingProtocolType = BrokerTest.getDefaultEncodingProtocolType();
		int port = 0;
		if (defaultEncodingProtocolType.equals(NetProtocolType.SOAP_v0))
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent2-legacy-udp-port"));
		}
		else
		{
			port = Integer.parseInt(ConfigurationInfo.getParameter("agent2-udp-port"));
		}
		return port;
	}

    public String getAgent1Hostname(){
        return ConfigurationInfo.getParameter("agent1-host");
    }

    public String getAgent2Hostname(){
        return ConfigurationInfo.getParameter("agent2-host");
    }

    @Parameterized.Parameters()
    public static Collection getProtocolTypes() {
        return Arrays.asList(new Object[][]{
            {NetProtocolType.JSON},
            {NetProtocolType.PROTOCOL_BUFFER},
            {NetProtocolType.THRIFT},
            {NetProtocolType.SOAP},
        });
    }


}

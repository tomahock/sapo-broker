package pt.com.broker.functests.helpers;


import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.ErrorListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetFault;

import java.util.concurrent.Future;

public class GenericNegativeTest extends BrokerTest
{

	private BrokerClient brokerClient = null;

	private boolean okToTimeout = false;

	private BrokerListener defaultErrorListener = new ErrorListenerAdapter() {


        @Override
        public void onMessage(NetFault message,HostInfo host) {
            System.out.println("Error Message Fault");
            faultFuture.set(message);
        }
    };


	private byte[] dataToSend = new byte[] {};

	private SetValueFuture<NetFault> faultFuture = new SetValueFuture<NetFault>();

	private String faultCode = null;
	private String faultMessage = null;
	private String faultActionId = null;
	private String faultDetail = null;

	public GenericNegativeTest(String testName)
	{
		super(testName);

		try
		{
			brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), getEncodingProtocolType());
            brokerClient.connect();
		}
		catch (Throwable e)
		{
			setReasonForFailure(e);
		}
	}

	@Override
	protected void build() throws Throwable
	{
		addAction();
		addConsequece();
	}


	protected void addConsequece()
	{
		FaultConsequence consequence = new FaultConsequence("fault receiver", "producer", faultFuture);
		consequence.setFaultCode(getFaultCode());
		consequence.setFaultMessage(getFaultMessage());

		addConsequences(consequence);

	}

	private void addAction()
	{
		setAction(new Action("message publisher", "producer")
		{

			@Override
			public Step run() throws Exception
			{
				try
				{
					brokerClient = getBrokerClient();

					brokerClient.setFaultListener(getErrorListener());

					//brokerClient.getNetHandler().getConnector().getOutput().write(getDataToSend());

                    byte[] data = getDataToSend();

                    Byte[] byteObjects = new Byte[data.length];

                    int i=0;

                    for(byte b: data)
                        byteObjects[i++] = b;  // Autoboxing.

                    Future f = brokerClient.getHosts().getAvailableHost().getChannel().writeAndFlush(byteObjects);

                    f.get();

					setDone(true);
					setSucess(true);
				}
				catch (Throwable t)
				{
					setReasonForFailure(t.toString());
				}
				return this;
			}

		});

	}

    @Override
    protected void end() {
        try {

            brokerClient.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBrokerClient(BrokerClient brokerClient)
	{
		this.brokerClient = brokerClient;
	}

	public BrokerClient getBrokerClient()
	{
		return brokerClient;
	}

	public void setDataToSend(byte[] dataToSend)
	{
		this.dataToSend = dataToSend;
	}

	public byte[] getDataToSend()
	{
		return dataToSend;
	}

	public void setFaultCode(String faultCode)
	{
		this.faultCode = faultCode;
	}

	public String getFaultCode()
	{
		return faultCode;
	}

	public void setFaultMessage(String faultMessage)
	{
		this.faultMessage = faultMessage;
	}

	public String getFaultMessage()
	{
		return faultMessage;
	}

	public void setErrorListener(BrokerListener defaultErrorListener)
	{
		this.defaultErrorListener = defaultErrorListener;
	}

	public BrokerListener getErrorListener()
	{
		return defaultErrorListener;
	}

	public void setFaultActionId(String faultActionId)
	{
		this.faultActionId = faultActionId;
	}

	public String getFaultActionId()
	{
		return faultActionId;
	}

	public void setFaultDetail(String faultDetail)
	{
		this.faultDetail = faultDetail;
	}

	public String getFaultDetail()
	{
		return faultDetail;
	}

	public void setOkToTimeout(boolean okToTimeout)
	{
		this.okToTimeout = okToTimeout;
	}

	public boolean isOkToTimeout()
	{
		return okToTimeout;
	}
}

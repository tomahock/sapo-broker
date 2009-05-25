package pt.com.broker.functests.helpers;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerErrorListenter;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.types.NetFault;

public class GenericNegativeTest extends BrokerTest
{

	private BrokerClient brokerClient = null;
	
	private boolean okToTimeout = false;
	
	private BrokerErrorListenter defaultErrorListener = new BrokerErrorListenter()
	{
		@Override
		public void onError(Throwable throwable)
		{
			System.err.println("Unexpected exception occurred. " + throwable);
		}

		@Override
		public void onFault(NetFault fault)
		{
			faultFuture.set(fault);
		}
	};
	
	private byte[] dataToSend = new byte[]{};
	
	private SetValueFuture<NetFault> faultFuture = new SetValueFuture<NetFault>();
	
	private String faultCode = null;
	private String faultMessage = null;
	private String faultActionId = null;
	private String faultDetail = null;
	
	
	
	public GenericNegativeTest(String testName)
	{
		super(testName);
	}

	@Override
	protected void build() throws Throwable
	{
		addAction();
		
		addConsequece();
	}

	private void addConsequece()
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
					brokerClient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", getEncodingProtocolType());

					
					
					brokerClient.setErrorListener(getErrorListener());

					brokerClient.getNetHandler().getConnector().getOutput().write(getDataToSend());
					
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

	public void setErrorListener(BrokerErrorListenter defaultErrorListener)
	{
		this.defaultErrorListener = defaultErrorListener;
	}

	public BrokerErrorListenter getErrorListener()
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
}

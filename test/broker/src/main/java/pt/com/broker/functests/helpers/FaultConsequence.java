package pt.com.broker.functests.helpers;

import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.types.NetFault;

public class FaultConsequence extends Consequence
{
	private boolean okToTimeout = false;

	private String faultCode;
	private String faultMessage;
	private String faultDetail;
	private String actionId;

	private SetValueFuture<NetFault> future;

	public FaultConsequence(String name, String actorName, SetValueFuture<NetFault> future)
	{
		super(name, actorName);

		this.future = future;
	}

	@Override
	public Step run() throws Exception
	{
		try
		{
			NetFault fault = future.get();
			if (faultCode != null)
			{
				if (!faultCode.equals(fault.getCode()))
				{
					String reason = String.format("Fault code mismatch! Expected: %s Received: %s", faultCode, fault.getCode());
					setReasonForFailure(reason);

					return this;
				}
			}
			if (faultMessage != null)
			{
				if (!faultMessage.equals(fault.getMessage()))
				{
					String reason = String.format("Fault message mismatch! Expected: %s Received: %s", faultMessage, fault.getMessage());
					setReasonForFailure(reason);

					return this;
				}
			}
			if (faultDetail != null)
			{
				if (!faultDetail.equals(fault.getDetail()))
				{
					String reason = String.format("Fault detail mismatch! Expected: %s Received: %s", faultDetail, fault.getDetail());
					setReasonForFailure(reason);

					return this;
				}
			}
			if (actionId != null)
			{
				if (!actionId.equals(fault.getActionId()))
				{
					String reason = String.format("Fault action id mismatch! Expected: %s Received: %s", actionId, fault.getActionId());
					setReasonForFailure(reason);

					return this;
				}
			}
			setDone(true);
			setSucess(true);
		}
		catch (Throwable t)
		{
			System.out.println(String.format("FaultConsequence.run() failed. Name: '%s'. Actor name: '%s'. Reason: %s", getName(), getActorName(), t.getMessage()));
			setReasonForFailure(t.toString());
		}
		return this;
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

	public void setFaultDetail(String faultDetail)
	{
		this.faultDetail = faultDetail;
	}

	public String getFaultDetail()
	{
		return faultDetail;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}
}

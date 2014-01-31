package pt.com.broker.functests;

import java.util.concurrent.Callable;

public abstract class Step implements Callable<Step>
{
	private String name;
	private String reasonForFailure;

	private boolean done = false;
	private boolean sucess = false;

	public Step(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	protected void setDone(boolean done)
	{
		this.done = done;
	}

	public boolean isDone()
	{
		return done;
	}

	protected void setSucess(boolean sucess)
	{
		this.sucess = sucess;
	}

	public boolean isSucess()
	{
		return sucess;
	}

	public boolean isFailure()
	{
		return (done) && (!sucess);
	}

	public void setReasonForFailure(String reasonForFailure)
	{
		setDone(true);
		setSucess(false);
		this.reasonForFailure = reasonForFailure;
	}

	public String getReaseonForFailure()
	{
		return reasonForFailure;
	}

	@Override
	public final Step call() throws Exception
	{
		if (isFailure())
			return this;

		return run();
	}

	public abstract Step run() throws Exception;
}

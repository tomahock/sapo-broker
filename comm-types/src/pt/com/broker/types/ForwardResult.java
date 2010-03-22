package pt.com.broker.types;


public class ForwardResult
{
	public final Result result;
	public final long time;

	public enum Result{ SUCCESS, FAILED, NOT_ACKNOWLEDGE};
	
	public ForwardResult(Result result)
	{
		this(result, -1);
	}
	
	public ForwardResult(Result result, long time)
	{
		this.result = result;
		this.time = time;
	}
}
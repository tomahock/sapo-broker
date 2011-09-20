package pt.com.broker.types;

public class ForwardResult
{
	public final Result result;
	public long time;

	public enum Result
	{
		SUCCESS, FAILED, NOT_ACKNOWLEDGE
	};

	public ForwardResult(Result result)
	{
		this(result, -1);
	}

	public ForwardResult(Result result, long time)
	{
		this.result = result;
		this.time = time;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForwardResult other = (ForwardResult) obj;
		if (result == null)
		{
			if (other.result != null)
				return false;
		}
		else if (!result.equals(other.result))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

}
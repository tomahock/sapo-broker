package pt.com.broker.functests;

public abstract class Consequence extends Step
{
	private String actorName;

	public Consequence(String name, String actorName)
	{
		super(name);
		this.actorName = actorName;
	}

	public String getActorName()
	{
		return actorName;
	}
}

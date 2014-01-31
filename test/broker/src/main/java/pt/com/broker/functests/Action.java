package pt.com.broker.functests;

public abstract class Action extends Step
{
	private String actorName;

	public Action(String name, String actorName)
	{
		super(name);
		this.actorName = actorName;
	}

	public String getActorName()
	{
		return actorName;
	}
}

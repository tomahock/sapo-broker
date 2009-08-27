package pt.com.broker.functests;

public class HelloWorldTest extends Test
{
	public HelloWorldTest()
	{
		super("Hello World");
	}

	@Override
	protected void build()
	{
		this.addPrerequisite(new Prerequisite("simple prerequisite")
		{
			public Step run() throws Exception
			{
				System.out.println("I'm a prerequisite and I'm running!!");
				// consumer.subscribe!

				Thread.sleep(3000);

				setDone(true);
				setSucess(true);

				return this;
			}

		});

		this.setAction(new Action("print", "producer")
		{
			public Step run() throws Exception
			{
				System.out.println("I'm an action and I'm running!!");
				setDone(true);
				setSucess(true);

				return this;
			}
		});

		this.addConsequences(new Consequence("Consequence 1", "consumer")
		{
			public Step run() throws Exception
			{
				System.out.println("I'm an consquence and I'm running!!");
				setDone(true);
				setSucess(true);

				return this;
			}
		});

		this.addConsequences(new Consequence("Consequence 2", "consumer")
		{
			public Step run() throws Exception
			{
				System.out.println("I'm another consquence and I'm running!!");
				setDone(true);
				setSucess(true);

				return this;
			}
		});

		this.addEpilogue(new Epilogue("Epilogue")
		{
			public Step run() throws Exception
			{
				System.out.println("I'm an eplilogue and I'm running!!");
				setDone(true);
				setSucess(true);

				return this;
			}
		});
	}
}

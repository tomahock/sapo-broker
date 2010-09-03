package pt.com.broker.functests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Test
{
	private static final Logger log = LoggerFactory.getLogger(Test.class);
	private static final ExecutorService executer = Executors.newFixedThreadPool(8);

	private String name;

	private boolean okToTimeOut = false;

	private List<Prerequisite> prerequisites = new ArrayList<Prerequisite>();
	private Action action;
	private List<Consequence> consequences = new ArrayList<Consequence>();
	private List<Epilogue> epilogues = new ArrayList<Epilogue>();

	private static long defaultTimeout = getDefaultTimeout();

	private long timeout = getDefaultTimeout();

	private boolean skipTest = false;

	public Test(String name)
	{
		this.name = name;
	}

	protected void addPrerequisite(Prerequisite prerequisite)
	{
		getPrerequisites().add(prerequisite);
	}

	protected void setAction(Action action)
	{
		this.action = action;
	}

	protected void addConsequences(Consequence consequence)
	{
		getConsequences().add(consequence);
	}

	protected void addEpilogue(Epilogue epilogue)
	{
		getEpilogues().add(epilogue);
	}

	protected abstract void build() throws Throwable;

	public final boolean run(int nrOfRuns, TestsResults testResults)
	{
		boolean result = true;
		if (skipTest())
		{
			System.out.println("test skiped");
			testResults.addSkipedTest(getNameAndEncoding(testResults));
			return true;
		}
		try
		{
			log.info("Building test - " + getName());
			build();

			log.info("Initializing  test - " + getName());
			for (Prerequisite prereq : getPrerequisites())
			{
				prereq.call();
			}

			log.info("Performing test - " + getName());

			ArrayList<Callable<Step>> activities = new ArrayList<Callable<Step>>(getConsequences().size() + 1);

			activities.add(getAction());
			activities.addAll(getConsequences());

			List<Future<Step>> executionResults;
			int count = nrOfRuns;
			do
			{
				executionResults = executer.invokeAll(activities, getTimeout(), TimeUnit.MILLISECONDS);

				for (Future<Step> executionResult : executionResults)
				{
					Step step = executionResult.get();
					if (executionResult.isDone())
					{
						if (step.isSucess())
						{
							log.info("Successfull step - " + step.getName());
						}
						else
						{
							log.info("##### :( ##### Unsuccessfull step - " + step.getName() + " Reason: " + step.getReaseonForFailure());
							result = false;
						}
					}
					else
					{
						log.info("##### :( ##### Step didn't complete - " + step.getName());
					}
				}
			}
			while ((--count) != 0);

		}
		catch (Throwable t)
		{
			if (!okToTimeOut() && (getAction() != null))
			{
				if (!getAction().isSucess())
				{
					System.out.println(String.format(">>>> Action failed. Reason: %s", getAction().getReaseonForFailure()));
				}
				for (Consequence consequence : getConsequences())
				{
					if (!consequence.isSucess())
					{
						System.out.println(String.format(">>>> Consequence '%s' failed. Reason: %s", consequence.getName(), consequence.getReaseonForFailure()));
					}
				}
			}

			if ((t instanceof CancellationException) && okToTimeOut())
			{
				result = true;
			}
			else
			{
				log.error("##### :( ##### Test " + getName() + " failed!", t);
				result = false;
			}
		}
		finally
		{
			log.info("Finalizing test - " + getName());
			for (Epilogue epilogue : getEpilogues())
			{
				try
				{
					if (!epilogue.call().isSucess())
						log.error("Epilogue step failed test without exception. Name: " + epilogue.getName());

				}
				catch (Throwable t)
				{
					log.error("Epilogue step failed test with exception. Name: " + epilogue.getName(), t);
				}
			}
		}
		if (result)
			testResults.addPositiveTest();
		else
			testResults.addFailedTest(getNameAndEncoding(testResults));

		return result;
	}

	private String getNameAndEncoding(TestsResults testResults)
	{
		String encoding = testResults.getProperty("Encoding");
		if (encoding == null)
			return getName();
		return getName() + " : " + encoding;
	}

	public final boolean run(TestsResults testResults)
	{
		return run(1, testResults);
	}

	public String getName()
	{
		return name;
	}

	public List<Prerequisite> getPrerequisites()
	{
		return prerequisites;
	}

	public Action getAction()
	{
		return action;
	}

	public List<Consequence> getConsequences()
	{
		return consequences;
	}

	private List<Epilogue> getEpilogues()
	{
		return epilogues;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public long getTimeout()
	{
		return timeout;
	}

	public static void setDefaultimeout(long timeout)
	{
		Test.defaultTimeout = timeout;
	}

	public static long getDefaultTimeout()
	{
		return defaultTimeout;
	}

	public void setSkipTest(boolean skipTest)
	{
		this.skipTest = skipTest;
	}

	public boolean skipTest()
	{
		return skipTest;
	}

	public void setOkToTimeOut(boolean okToTimeOut)
	{
		this.okToTimeOut = okToTimeOut;
	}

	public boolean okToTimeOut()
	{
		return okToTimeOut;
	}
}

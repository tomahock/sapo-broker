package pt.com.broker.functests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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

	private List<Prerequisite> prerequisites = new ArrayList<Prerequisite>();
	private Action action;
	private List<Consequence> consequences = new ArrayList<Consequence>();
	private List<Epilogue> epilogues = new ArrayList<Epilogue>();
	
	private long timeout = 2 * 60 * 1000; 
	
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

	public final boolean run(int nrOfRuns)
	{
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
			
			ArrayList< Callable<Step> > activities = new ArrayList< Callable<Step> >(getConsequences().size() +1 );
			
			activities.add(getAction());
			activities.addAll(getConsequences());
			
			List<Future<Step>> executionResults = executer.invokeAll(activities, getTimeout(), TimeUnit.MILLISECONDS);

			for(Future<Step> result : executionResults)
			{
				Step step = result.get();
				if(result.isDone())
				{
					if(step.isSucess())
					{
						log.info("Successfull step - " + step.getName());
					}
					else
					{
						log.info("##### :( ##### Unsuccessfull step - " + step.getName() + " Reason: " + step.getReaseonForFailure());
					}
				} else {
					log.info("##### :( ##### Step didn't complete - " + step.getName());
				}
			}
			
		}
		catch (Throwable t)
		{
			log.error("##### :( ##### Test " + getName() + " failed!", t);
			return false;
		}
		finally
		{
			log.info("Finalizing test - " + getName());
			for (Epilogue epilogue : getEpilogues())
			{
				try{
					if(! epilogue.call().isSucess() )
						log.error("Epilogue step failed test without exception. Name: " + epilogue.getName());
					
				}catch(Throwable t)
				{
					log.error("Epilogue step failed test with exception. Name: " + epilogue.getName(), t);
				}
			}
		}
		return true;
	}

	public final boolean run()
	{
		return run(1);
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

	public List<Epilogue> getEpilogues()
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
}

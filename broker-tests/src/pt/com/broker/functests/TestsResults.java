package pt.com.broker.functests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestsResults
{
	private AtomicInteger positiveTests = new AtomicInteger();
	private AtomicInteger negativeTests = new AtomicInteger();
	private List<String> badTests = new ArrayList<String>();
	private List<String> skippedTests = new ArrayList<String>();
	public void addPositiveTest()
	{
		positiveTests.addAndGet(1);
	}

	public void addFailedTest(String testName)
	{
		negativeTests.addAndGet(1);
		badTests.add(testName);
	}

	public void addSkipedTest(String name)
	{
		skippedTests.add(name);
	}

	public int getPositiveTestsCount()
	{
		return positiveTests.get();
	}

	public int getFailedTestsCount()
	{
		return negativeTests.get();
	}

	public int getSkippedTestsCount()
	{
		return skippedTests.size();
	}

	public List<String> getFailedTests()
	{
		return badTests;
	}

	public List<String> getSkippedTests()
	{
		return skippedTests;
	}
	
	/***
	 * Total number of tests performed (skipped tests are excluded).
	 * 
	 * @return Positive plus negative tests.
	 */
	public int getTotalTests()
	{
		return getPositiveTestsCount() + getFailedTestsCount();
	}
}

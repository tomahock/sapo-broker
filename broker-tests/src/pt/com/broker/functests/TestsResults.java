package pt.com.broker.functests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestsResults
{
	private AtomicInteger positiveTests = new AtomicInteger();
	private AtomicInteger negativeTests = new AtomicInteger();
	private List<String> badTests = new ArrayList<String>();
	private List<String> skippedTests = new ArrayList<String>();

	public Map<String, String> properties = new HashMap<String, String>();

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

	public void addProperty(String name, String value)
	{
		properties.put(name, value);
	}

	public String getProperty(String name)
	{
		return properties.get(name);
	}

	public void deleteProperty(String name)
	{
		properties.remove(name);
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

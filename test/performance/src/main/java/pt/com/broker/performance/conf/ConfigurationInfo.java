package pt.com.broker.performance.conf;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.caudexorigo.Shutdown;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationInfo
{
	private static final Logger log = LoggerFactory.getLogger(ConfigurationInfo.class);

	private static TestParams testParams = null;

	private static Map<String, Map<String, String>> parameters = new HashMap<String, Map<String, String>>();

	static
	{
		JAXBContext jc;
		Unmarshaller u = null;
		String filename = System.getProperty("config-file");
		if (StringUtils.isBlank(filename))
		{
			log.error("Fatal error: No configuration file defined. Please set the enviroment variable 'config-path' to valid path for the configuration file");
			Shutdown.now();
		}
		try
		{
			jc = JAXBContext.newInstance("pt.com.broker.performance.conf");
			u = jc.createUnmarshaller();
			File f = new File(filename);
			boolean b = f.exists();
			if (!b)
			{
				log.error("Configuration file (" + filename + ") was not found.");
			}
			testParams = (TestParams) u.unmarshal(f);
		}
		catch (Throwable e)
		{
			testParams = null;
			log.error("Configuration initialization failed.", e);
		}
	}

	private static final String DEFAULT_NAME = "default";

	public static void init()
	{
		// defaults test params
		addParams(DEFAULT_NAME, testParams.getDefaults().getParam());

		// test specific
		for (TestParams.Tests.Test test : testParams.getTests().getTest())
		{
			addParams(test.getTestName(), test.getParam());
		}

		testParams = null;
	}

	private static void addParams(String testName, List<Param> parameters)
	{
		if (ConfigurationInfo.parameters.containsValue(testName))
		{
			log.error("Trying to add the already existent test: " + testName);
			return;
		}

		HashMap<String, String> params = new HashMap<String, String>(parameters.size());
		for (Param para : parameters)
		{
			params.put(para.getParamName(), para.getParamValue());
		}

		ConfigurationInfo.parameters.put(testName, params);
	}

	public static String getParameter(String paramName)
	{
		return getParameter(DEFAULT_NAME, paramName);
	}

	public static String getParameter(String testName, String paramName)
	{
		Map<String, String> test = ConfigurationInfo.parameters.get(testName);
		if (test == null)
			return null;
		String paramValue = test.get(paramName);

		return paramValue; // may be null
	}

}

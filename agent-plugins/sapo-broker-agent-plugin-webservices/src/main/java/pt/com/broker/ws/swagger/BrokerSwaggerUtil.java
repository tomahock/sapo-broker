package pt.com.broker.ws.swagger;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.model.ApiInfo;

public class BrokerSwaggerUtil
{

	public static final String API_VERISON = "1.0.0";

	public static final String API_BASE_PATH = "http://localhost:3381/broker";
	public static final String API_INFO_TITLE = "Broker Agent Rest API";
	public static final String API_INFO_DESC = "API to manage this broker agent instance.";
	public static final String API_TERMS_URL = "";
	public static final String API_DEV_CONTACT = "bruno.e.oliveira@telecom.pt";
	public static final String API_LICENCE = "";
	public static final String API_LICENCE_URL = "";

	public static final ApiInfo getApiInfo()
	{
		ApiInfo info = new ApiInfo(
				API_INFO_TITLE,
				API_INFO_DESC,
				API_TERMS_URL,
				API_DEV_CONTACT,
				API_LICENCE,
				API_LICENCE_URL
				);
		return info;
	}

	public static final BeanConfig getBeanConfig()
	{
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion(API_VERISON);
		beanConfig.setResourcePackage("pt.com.broker.ws.rest");
		beanConfig.setBasePath(API_BASE_PATH);
		beanConfig.setDescription("API to monitor and interact with the broker agent.");
		beanConfig.setTitle("Broker agent RESTful api");
		beanConfig.setScan(true);
		return beanConfig;
	}

}

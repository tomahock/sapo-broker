package pt.com.xml;

import java.util.Date;

import org.caudexorigo.text.DateUtil;

public class Status
{
	public String message;

	public String timestamp;

	public String version;

	public Status()
	{
		message = "Agent is alive";
		timestamp = DateUtil.formatISODate(new Date());
		version = "3.0";
	}
}

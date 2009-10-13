package pt.com.broker.codec.xml;

import java.util.Date;

import org.caudexorigo.text.DateUtil;

public class Status
{
	public String message;

	public String timestamp;

	public Status()
	{
		message = ":)";
		timestamp = DateUtil.formatISODate(new Date());
	}
}

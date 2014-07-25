package pt.com.broker.codec.xml;

import org.caudexorigo.time.ISO8601;

import java.util.Date;

public class Status
{
	public String message;

	public String timestamp;

	public Status()
	{
		message = ":)";
		timestamp = ISO8601.format(new Date());
	}
}

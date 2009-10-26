package pt.com.broker.monitorization;

import java.util.Date;

import org.caudexorigo.text.DateUtil;

public class Utils
{
	public static String formatDate(long time)
	{
		Date date = new Date(time);

		return DateUtil.formatISODate(date);
	}
}

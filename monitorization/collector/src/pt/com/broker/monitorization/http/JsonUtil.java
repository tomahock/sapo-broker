package pt.com.broker.monitorization.http;

import java.util.Collection;
import java.util.Iterator;

import pt.com.broker.monitorization.actions.JsonEncodable;

public class JsonUtil
{
	public static String getJsonEncodedCollection(Collection<JsonEncodable> items)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Iterator<JsonEncodable> iterator = items.iterator();
		if (items.size() != 0)
		{
			for (int i = 0; i != items.size() - 1; ++i)
			{
				JsonEncodable item = iterator.next();
				String s = item.toJson();
				sb.append(s);
				sb.append(",");
			}

			JsonEncodable item = iterator.next();
			sb.append(item.toJson());
		}
		sb.append("]");
		return sb.toString();
	}
}

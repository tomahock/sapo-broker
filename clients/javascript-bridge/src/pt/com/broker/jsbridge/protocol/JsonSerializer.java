package pt.com.broker.jsbridge.protocol;

import org.caudexorigo.io.UnsynchronizedStringReader;
import org.caudexorigo.io.UnsynchronizedStringWriter;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonSerializer
{
	private static final ObjectMapper mapper = new ObjectMapper();

	public static String toJson(JsonMessage bmsg) throws Throwable
	{
		if (bmsg != null)
		{
			UnsynchronizedStringWriter sw = new UnsynchronizedStringWriter();
			mapper.writeValue(sw, bmsg);
			return sw.toString();
		}
		else
		{
			throw new NullPointerException("Can not serialize null message");
		}
	}

	public static JsonMessage fromJson(String json_message) throws Throwable
	{
		return mapper.readValue(new UnsynchronizedStringReader(json_message), JsonMessage.class);
	}

	public static void main(String[] args) throws Throwable
	{

		JsonMessage sr = new JsonMessage(JsonMessage.MessageType.SUBSCRIBE, "/topic/foo");
		// sr.setPayload("This is a test");

		String json = toJson(sr);

		System.out.println(json);

		JsonMessage b = fromJson(json);

		System.out.println(b.getAction());
		System.out.println(b.getPayload());
	}
}

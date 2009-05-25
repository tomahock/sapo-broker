/**
 * 
 */
package pt.com.broker.auth.saposts;

import java.util.Map;
import java.util.TreeMap;

public class SapoSTSCodeErrors
{
	private static Map<String, String> codeErrors = new TreeMap<String, String>();

	static
	{
		codeErrors.put("2610", "Invalid Token");
		codeErrors.put("1010", "Invalid Credentials");
		codeErrors.put("2620", "Token Expired");
	}

	public static String getErrorDescription(String errorCode)
	{
		String val = codeErrors.get(errorCode);
		return val != null ? val : "Unknown error";
	}
}
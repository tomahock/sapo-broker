/**
 * 
 */
package pt.com.gcs.messaging;

import java.util.Iterator;

import pt.com.types.NetAction;
import pt.com.types.NetParameter;

public abstract class GcsMessageAdapter
{
	public abstract Iterator<NetParameter> getHeaders();

	public abstract NetAction getAction();
}

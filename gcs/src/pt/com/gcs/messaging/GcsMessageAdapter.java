/**
 * 
 */
package pt.com.gcs.messaging;

import java.util.Iterator;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetParameter;

public abstract class GcsMessageAdapter
{
	public abstract Iterator<NetParameter> getHeaders();

	public abstract NetAction getAction();
}

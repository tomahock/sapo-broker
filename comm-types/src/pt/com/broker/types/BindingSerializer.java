package pt.com.broker.types;

import java.io.OutputStream;

/**
 * BindingSerializer interface is implemented by the different protocol types and used as an abstraction to encoded/decode NetMessage instances.
 *
 */

public interface BindingSerializer
{

	public abstract NetMessage unmarshal(byte[] packet);

	public abstract byte[] marshal(NetMessage message);

	public abstract void marshal(NetMessage message, OutputStream out);

}
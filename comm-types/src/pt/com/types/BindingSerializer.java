package pt.com.types;

import java.io.OutputStream;

public interface BindingSerializer
{

	public abstract NetMessage unmarshal(byte[] packet);

	public abstract byte[] marshal(NetMessage message);

	public abstract void marshal(NetMessage message, OutputStream out);

}
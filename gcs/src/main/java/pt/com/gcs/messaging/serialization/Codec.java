package pt.com.gcs.messaging.serialization;

public interface Codec<T>
{

	public T unmarshall(byte[] b) throws Throwable;

	public byte[] marshall(T obj) throws Throwable;

}

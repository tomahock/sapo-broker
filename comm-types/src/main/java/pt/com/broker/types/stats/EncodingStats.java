package pt.com.broker.types.stats;

import java.util.concurrent.atomic.AtomicLong;

public class EncodingStats
{
	// Xml
	private static final AtomicLong soapEncoded = new AtomicLong(0);

	public static void newSoapEncodedMessage()
	{
		soapEncoded.incrementAndGet();
	}

	public static long getSoapEncodedMessageAndReset()
	{
		return soapEncoded.getAndSet(0);
	}

	private static final AtomicLong soapDecoded = new AtomicLong(0);

	public static void newSoapDecodedMessage()
	{
		soapDecoded.incrementAndGet();
	}

	public static long getSoapDecodedMessageAndReset()
	{
		return soapDecoded.getAndSet(0);
	}

	// Protobuf
	private static final AtomicLong protoEncoded = new AtomicLong(0);

	public static void newProtoEncodedMessage()
	{
		protoEncoded.incrementAndGet();
	}

	public static long getProtoEncodedMessageAndReset()
	{
		return protoEncoded.getAndSet(0);
	}

	private static final AtomicLong protoDecoded = new AtomicLong(0);

	public static void newProtoDecodedMessage()
	{
		protoDecoded.incrementAndGet();
	}

	public static long getProtoDecodedMessageAndReset()
	{
		return protoDecoded.getAndSet(0);
	}

	// Thrift
	private static final AtomicLong thriftEncoded = new AtomicLong(0);

	public static void newThriftEncodedMessage()
	{
		thriftEncoded.incrementAndGet();
	}

	public static long getThriftEncodedMessageAndReset()
	{
		return thriftEncoded.getAndSet(0);
	}

	private static final AtomicLong thriftDecoded = new AtomicLong(0);

	public static void newThriftDecodedMessage()
	{
		thriftDecoded.incrementAndGet();
	}

	public static long getThriftDecodedMessageAndReset()
	{
		return thriftDecoded.getAndSet(0);
	}
}
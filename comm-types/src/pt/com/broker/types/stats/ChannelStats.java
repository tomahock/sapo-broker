package pt.com.broker.types.stats;

import java.util.concurrent.atomic.AtomicLong;

public class ChannelStats
{
	// http
	private static final AtomicLong httpReceivedMessages = new AtomicLong(0);

	public static void newHttpMessageReceived()
	{
		httpReceivedMessages.incrementAndGet();
	}

	public static long getHttpReceivedMessagesAndReset()
	{
		return httpReceivedMessages.getAndSet(0);
	}

	// dropbox
	private static final AtomicLong dropboxReceivedMessages = new AtomicLong(0);

	public static void newDropboxMessageReceived()
	{
		dropboxReceivedMessages.incrementAndGet();
	}

	public static long getDropboxReceivedMessagesAndReset()
	{
		return dropboxReceivedMessages.getAndSet(0);
	}
}
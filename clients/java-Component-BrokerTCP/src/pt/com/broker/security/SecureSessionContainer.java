package pt.com.broker.security;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.concurrent.CustomExecutors;

public class SecureSessionContainer
{
	private static final ScheduledExecutorService shed_exec = CustomExecutors.newScheduledThreadPool(1, "SessionsJanitor");
	private static AtomicInteger sessionCounter;

	private static Map<String, SecureSessionInfo> initializingSecureSession;
	private static Map<String, Long> initializaionTimeOuts; // protected by initializingSecureSession lock
	private static Map<String, String> sessionIdentifiersMatch;


	static
	{
		try
		{
			sessionCounter = new AtomicInteger(0);
			initializingSecureSession = new TreeMap<String, SecureSessionInfo>();
			initializaionTimeOuts = new TreeMap<String, Long>(); // protected by initializingSecureSession lock
			sessionIdentifiersMatch = new TreeMap<String, String>();

			Runnable unfinishedSessionsJanitor = new Runnable()
			{
				public void run()
				{
					System.out.println("SecureSessionContainer.run()");

					long currentTime = System.currentTimeMillis();
					boolean deletedSomeEntry = false;

					do
					{

						synchronized (initializingSecureSession)
						{
							for (Map.Entry<String, Long> entry : initializaionTimeOuts.entrySet())
							{
								if (entry.getValue().longValue() > currentTime)
								{
									initializingSecureSession.remove(entry.getKey());
									initializaionTimeOuts.remove(entry.getKey());

									deletedSomeEntry = true;
									break;
								}
							}
						}
					}
					while (deletedSomeEntry);
				}

			};

			shed_exec.schedule(unfinishedSessionsJanitor, 2, TimeUnit.MINUTES);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			// This exception should never occur
		}
	}

	public static String getLocalCommunicationId()
	{
		int val = sessionCounter.getAndAdd(1);
		return "SecureSession_" + val;
	}

	public static void addInitializingSecureSessionInfo(SecureSessionInfo secureSessionInfo)
	{
		synchronized (initializingSecureSession)
		{
			initializingSecureSession.put(secureSessionInfo.getLocalCommunicationId(), secureSessionInfo);
			initializaionTimeOuts.put(secureSessionInfo.getLocalCommunicationId(), System.currentTimeMillis() + 3 * 1000);
		}
	}

	public static void removeInitializingSecureSessionInfo(String localCommunicationId, String globalCommunicationId)
	{
		synchronized (initializingSecureSession)
		{
			initializingSecureSession.remove(localCommunicationId);
			initializaionTimeOuts.remove(localCommunicationId);
		}
		synchronized (sessionIdentifiersMatch)
		{
			sessionIdentifiersMatch.remove(globalCommunicationId);
		}
	}

	public static SecureSessionInfo getInitializingSecureSessionInfo(String localCommunicationId)
	{
		synchronized (initializingSecureSession)
		{
			return initializingSecureSession.get(localCommunicationId);
		}
	}

	public static void associateCommunicationId(String localId, String globalCommunicationId)
	{
		synchronized (sessionIdentifiersMatch)
		{
			sessionIdentifiersMatch.put(globalCommunicationId, localId);
		}
	}

	public static String getLocalCommunicationId(String globalCommunicationId)
	{
		synchronized (sessionIdentifiersMatch)
		{
			return sessionIdentifiersMatch.get(globalCommunicationId);
		}
	}
}

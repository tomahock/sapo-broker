package pt.com.broker.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.DateUtil;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.auth.AccessControl.ValidationResult;
import pt.com.broker.auth.Session;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.codec.xml.SoapEnvelope;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.stats.ChannelStats;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

/**
 * FilePublisher deals with dropbox functionality publishing messages
 */
public class FilePublisher
{
	private static Logger log = LoggerFactory.getLogger(FilePublisher.class);

	private static FilePublisher instance = new FilePublisher();

	private static final long INITIAL_DELAY = 10L; // 10 segundos

	private static final long DEFAULT_CHECK_DELAY = 60L; // 60 segundos

	private static final long PUBLISH_PERIOD = 60 * 1000; // 1 minute

	private static final long PUBLISH_EVERY_CHECKS = 30;

	private final String dir;

	private final boolean isEnabled;

	private final long check_interval;

	private File dropBoxDir;

	private int fileCount = 0;

	volatile private long checks;

	private FilePublisher()
	{
		isEnabled = GcsInfo.isDropboxEnabled();
		dir = GcsInfo.getDropBoxDir();
		check_interval = GcsInfo.getDropBoxCheckInterval() > 0 ? GcsInfo.getDropBoxCheckInterval() : DEFAULT_CHECK_DELAY;

		log.debug("DropBox Monitor, dir: {}", dir);
		log.debug("DropBox Monitor, enabled: {}", isEnabled);
		log.debug("DropBox Monitor, check interval: {}", check_interval);

	}

	final FileFilter fileFilter = new FileFilter()
	{
		public boolean accept(File file)
		{
			fileCount++;
			return file.getName().endsWith(".good");
		}
	};

	final Comparator<File> fileComparator = new Comparator<File>()
	{
		private Collator c = Collator.getInstance(Locale.ENGLISH);

		public int compare(File f1, File f2)
		{
			if (f1 == f2)
				return 0;

			if (f1.isDirectory() && f2.isFile())
				return -1;
			if (f1.isFile() && f2.isDirectory())
				return 1;

			return c.compare(f1.getName(), f2.getName());
		}
	};

	private final Session filePublisherSession = new Session();
	volatile private long previousPublish = 0;
	private SoapBindingSerializer soapBindingSerializer = new SoapBindingSerializer();

	final Runnable publisher = new Runnable()
	{
		byte[] buffer = new byte[1024];

		public void run()
		{
			log.debug("Checking for files in the DropBox.");
			try
			{
				fileCount = 0;
				File[] files = dropBoxDir.listFiles(fileFilter);
				if (files == null)
				{
					return;
				}
				int goodFileCount = files.length;

				publishInfo(goodFileCount);

				if ((files != null) && (files.length > 0))
				{
					if (log.isDebugEnabled())
					{
						log.debug("Will try to publish " + files.length + " file(s).");
					}

					Arrays.sort(files, fileComparator);

					for (File msgf : files)
					{
						FileInputStream fis = new FileInputStream(msgf);

						byte[] inputFileData = new byte[1024];
						int dataRead = 0;

						NetMessage netMessage = null;

						boolean isFileValid = false;
						try
						{
							while (fis.available() != 0)
							{
								int count = fis.read(buffer);
								if ((inputFileData.length - dataRead) <= count)
								{
									inputFileData = Arrays.copyOf(inputFileData, inputFileData.length * 2);
								}
								System.arraycopy(buffer, 0, inputFileData, dataRead, count);
								dataRead += count;
							}

							inputFileData = Arrays.copyOf(inputFileData, dataRead);
							netMessage = soapBindingSerializer.unmarshal(inputFileData);

							isFileValid = true;
						}
						catch (Throwable e)
						{
							Throwable tr = ErrorAnalyser.findRootCause(e);
							CriticalErrors.exitIfCritical(tr);
							log.error("Error processing file \"" + msgf.getAbsolutePath() + "\". Error message: " + tr.getMessage());
						}

						if (isFileValid)
						{
							try
							{
								ValidationResult validationResult = AccessControl.validate(netMessage, filePublisherSession);
								if (validationResult.accessGranted)
								{
									if (netMessage.getAction().getActionType().equals(NetAction.ActionType.PUBLISH))
									{
										ChannelStats.newDropboxMessageReceived();

										if (netMessage.getAction().getPublishMessage().getDestinationType() == NetAction.DestinationType.TOPIC)
										{
											BrokerProducer.getInstance().publishMessage(netMessage.getAction().getPublishMessage(), MQ.requestSource(netMessage));
										}
										else if (netMessage.getAction().getPublishMessage().getDestinationType() == NetAction.DestinationType.QUEUE)
										{
											BrokerProducer.getInstance().enqueueMessage(netMessage.getAction().getPublishMessage(), MQ.requestSource(netMessage));
										}

									}
									else
									{
										log.error("Error publishing file \"" + msgf.getAbsolutePath() + "\". Not a publish message.");
									}
								}
								else
								{
									log.error("Message could not be published because it violated current ACL.");
								}
							}
							catch (Throwable e)
							{
								Throwable tr = ErrorAnalyser.findRootCause(e);
								CriticalErrors.exitIfCritical(tr);
								log.error("Error publishing file \"" + msgf.getAbsolutePath() + "\". Error message: " + tr.getMessage());
							}
							finally
							{
								try
								{
									fis.close();
									msgf.delete();
								}
								catch (Throwable t)
								{
									log.error("Error deleting file", t);
								}
							}
						}
						else
						{
							fis.close();
							File badFile = new File(msgf.getAbsolutePath() + ".bad");
							msgf.renameTo(badFile);
						}
					}
				}
				else
				{
					log.debug("No files to publish.");
				}
			}
			catch (Throwable e)
			{
				log.error(e.getMessage(), e);
			}
		}
	};

	public static void init()
	{
		if (instance.isEnabled)
		{
			instance.dropBoxDir = new File(instance.dir);
			if (instance.dropBoxDir.isDirectory())
			{
				BrokerExecutor.scheduleWithFixedDelay(instance.publisher, INITIAL_DELAY, instance.check_interval, TimeUnit.SECONDS);
				log.info("Drop box functionality enabled.");
			}
			else
			{
				abort();
			}
		}
		else
		{
			abort();
		}
	}

	private static void abort()
	{
		log.info("Drop box functionality disabled.");
	}

	public static String requestSource(SoapEnvelope soap)
	{
		if (soap.header != null)
			if (soap.header.wsaFrom != null)
				if (StringUtils.isNotBlank(soap.header.wsaFrom.address))
					return soap.header.wsaFrom.address;
		return null;
	}

	private void publishInfo(int goodFileCount)
	{
		long now = System.currentTimeMillis();
		boolean publish = false;

		if ((now > (previousPublish + PUBLISH_PERIOD)))
		{
			publish = true;

			++checks;
		}
		else if (((++checks) % PUBLISH_EVERY_CHECKS) == 0)
		{
			publish = true;
		}

		if (publish)
		{
			StringBuilder sb = new StringBuilder();
			String topic = String.format("/system/stats/dropbox/#%s#", GcsInfo.getAgentName());
			sb.append(String.format("<mqinfo date=\"%s\" agent-name=\"%s\">", DateUtil.formatISODate(new Date(now)), GcsInfo.getAgentName()));

			sb.append(String.format("\n	<item subject=\"dropbox\" predicate=\"count\" value=\"%s\" />", fileCount));

			sb.append("\n</mqinfo>");

			NetPublish np = new NetPublish(topic, DestinationType.TOPIC, new NetBrokerMessage(sb.toString()));
			Gcs.publish(np);
			previousPublish = now;
		}
	}
}

package pt.com.gcs.messaging;

import pt.com.gcs.messaging.serialization.MessageMarshaller;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

/**
 * BDBMessageKeyCreator implements SecondaryKeyCreator witch is used for extracting single-valued secondary keys from primary records.
 * 
 */

public class BDBMessageKeyCreator implements SecondaryKeyCreator
{
	@Override
	public boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry, DatabaseEntry resultEntry) throws DatabaseException
	{
		byte[] bdata = dataEntry.getData();
		BDBMessage bdbm;
		try
		{
			bdbm = MessageMarshaller.unmarshallBDBMessage(bdata);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return false;
		}

		String msgId = bdbm.getMessage().getAction().getNotificationMessage().getMessage().getMessageId();
		StringBinding.stringToEntry(msgId, resultEntry);

		return true;
	}
}

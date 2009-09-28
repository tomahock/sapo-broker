
using System;

namespace SapoBrokerClient
{
	/// <summary>
	/// IMessageAcceptedListener interface should be implemented by those who wish to be notified of an event related with messages that carry an action identifier.
	/// </summary>
	
	public interface IMessageAcceptedListener
	{
		void MessageAccepted(String ActionId);

		void MessageTimedout(String actionId);

		/**
		 * An error occurred during message processing.
		 * @param fault Error description
		 */
		void MessageFailed(NetFault fault);
	}
}

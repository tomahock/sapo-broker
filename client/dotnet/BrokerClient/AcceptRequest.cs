
using System;

namespace SapoBrokerClient
{
	/// <summary>
	/// AcceptRequest is meat to be used by clients that specify an action identifier.
	/// </summary>
	
	public class AcceptRequest
	{
		private String actionId;
		private IMessageAcceptedListener listener;
		private double timeout;

		/// <summary>
		/// Creates an instance of AcceptRequest.
		/// </summary>
		/// <param name="actionId">
		/// Message action identifier <see cref="System.String"/>
		/// </param>
		/// <param name="listner">
		/// An implementation of IMessageAcceptedListener <see cref="IMessageAcceptedListener"/>
		/// </param>
		/// <param name="timeout">
		/// A time interval, in milliseconds, during witch the Accept message is expected. <see cref="System.Int64"/>
		/// </param>
		public AcceptRequest(String actionId, IMessageAcceptedListener listner, long timeout)
		{
			if (actionId == null)
				throw new ArgumentNullException("actionId is null");
			if (listner == null)
				throw new ArgumentNullException("listner is null");
			if (timeout <= 0)
				throw new ArgumentOutOfRangeException("timeout <= 0");
			
			this.actionId = actionId;
			this.listener = listner;
			this.timeout = timeout;
		}
		
		public string ActionId {
			get {
				return actionId;
			}
		}

		public IMessageAcceptedListener Listener {
			get {
				return listener;
			}
		}

		public double Timeout {
			get {
				return timeout;
			}
		}
	}
}

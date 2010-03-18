using System;
using System.Collections.Generic;
using System.Text;

using System.Threading;


namespace SapoBrokerClient.Messaging
{
    /// <summary>
    /// PendingAcceptRequestsManager deals with Accept requests made but still unanswered holding them and setting them as timeout if necessary. The timeout may be up to 500 milliseconds overdue.
    /// </summary>
	class PendingAcceptRequestsManager
	{
        private class AcceptRequestTimeout
        {
            public AcceptRequest Request;
            public DateTime EfectiveTimeout;
        }

        private static IDictionary<string, AcceptRequestTimeout> pendingRequests = new Dictionary<string, AcceptRequestTimeout>();

        private static void FireOnTimeOut(AcceptRequest request)
        {
            request.Listener.MessageTimedout(request.ActionId);
        }
	
        private static Timer timer = new Timer(delegate(object state)
            {
                DateTime now = DateTime.Now;
                List<AcceptRequest> requests = new List<AcceptRequest>();
                lock (pendingRequests)
                {
                    foreach(KeyValuePair<string, AcceptRequestTimeout> pair in pendingRequests) 
                    {
                        if( pair.Value.EfectiveTimeout >= now )
                        {
                            requests.Add(pair.Value.Request);
                        }
                    }
                    foreach( AcceptRequest request in requests)
                    {
                        pendingRequests.Remove(request.ActionId);
                    }
                }
                // not sync
                foreach (AcceptRequest request in requests)
                {
                    FireOnTimeOut(request);
                }
            }
            , null, 5000, 500);

        #region Public members

        public static void AddAcceptRequest(AcceptRequest request)
        {
			if( request.ActionId == null)
                throw new Exception("Action identifier in AcceptRequest can not be null.");

            if( request.Listener == null)
                throw new Exception("MessageAcceptedListener in AcceptRequest can not be null.");

            lock (pendingRequests)
            {
                if( pendingRequests.ContainsKey(request.ActionId) )
                    throw new Exception(String.Format("Action indentifier \"{0}\" already beeing used.", request.ActionId));
               
                pendingRequests.Add(request.ActionId, new AcceptRequestTimeout { Request = request, EfectiveTimeout = System.DateTime.Now.AddMilliseconds(request.Timeout) });
            }
        }

        public static void MessageReceived(string actionId)
        {
            AcceptRequestTimeout request = null;
            lock (pendingRequests)
            {


                if (pendingRequests.ContainsKey(actionId))
                {
                    request = pendingRequests[actionId];
                    pendingRequests.Remove(actionId);
                }
            }
            if (request != null)
                request.Request.Listener.MessageAccepted(request.Request.ActionId);
        }

        /// <summary>
        /// Method called when NetFault message is received.
        /// </summary>
        /// <param name="fault">Fault message</param>
        /// <returns>returns true if the message has handled</returns>
        public static bool MessageFailed(NetFault fault)
        {
            string actionId = fault.ActionId;
            if( actionId == null)
                return false;
            AcceptRequestTimeout request;
            lock (pendingRequests)
            {
                request = pendingRequests[actionId];
                if (request != null)
                    pendingRequests.Remove(actionId);
            }
            if (request != null)
            {
                request.Request.Listener.MessageFailed(fault);
                return true;
            }

            return false;
        }

        #endregion

    }
}

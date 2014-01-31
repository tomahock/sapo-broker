
using System;
using System.Collections.Generic;

namespace SapoBrokerClient
{

    public class NetMessage
    {

        private NetAction action;

        public NetAction Action
        {
            get { return action; }
        }
        private IDictionary<string, string> headers;

        public IDictionary<string, string> Headers
        {
            get { return headers; }
        }

        public NetMessage(NetAction action)
            : this(action, null)
        {
        }

        public NetMessage(NetAction action, IDictionary<string, string> headers)
        {
            this.action = action;
            if (headers != null)
                this.headers = headers;
            else
                this.headers = new Dictionary<string, string>();
        }
    }

    public class NetAction
    {

        public enum ActionType
        {
            PUBLISH, POLL, ACCEPTED, ACKNOWLEDGE, SUBSCRIBE, UNSUBSCRIBE, NOTIFICATION, FAULT, PING, PONG, AUTH
        };

        public enum DestinationType
        {
            TOPIC, QUEUE, VIRTUAL_QUEUE
        };

        private ActionType actionType;

        public ActionType Action
        {
            get { return actionType; }
        }

        private NetPublish publishMessage;

        public NetPublish PublishMessage
        {
            get { return publishMessage; }
            set { publishMessage = value; }
        }
        private NetPoll pollMessage;

        public NetPoll PollMessage
        {
            get { return pollMessage; }
            set { pollMessage = value; }
        }
        private NetAccepted acceptedMessage;

        public NetAccepted AcceptedMessage
        {
            get { return acceptedMessage; }
            set { acceptedMessage = value; }
        }
        private NetAcknowledge acknowledgeMessage;

        public NetAcknowledge AcknowledgeMessage
        {
            get { return acknowledgeMessage; }
            set { acknowledgeMessage = value; }
        }
        private NetSubscribe subscribeMessage;

        public NetSubscribe SubscribeMessage
        {
            get { return subscribeMessage; }
            set { subscribeMessage = value; }
        }
        private NetUnsubscribe unsbuscribeMessage;

        public NetUnsubscribe UnsbuscribeMessage
        {
            get { return unsbuscribeMessage; }
            set { unsbuscribeMessage = value; }
        }
        private NetNotification notificationMessage;

        public NetNotification NotificationMessage
        {
            get { return notificationMessage; }
            set { notificationMessage = value; }
        }
        private NetFault faultMessage;

        public NetFault FaultMessage
        {
            get { return faultMessage; }
            set { faultMessage = value; }
        }
        private NetPing pingMessage;

        public NetPing PingMessage
        {
            get { return pingMessage; }
            set { pingMessage = value; }
        }
        private NetPong pongMessage;

        public NetPong PongMessage
        {
            get { return pongMessage; }
            set { pongMessage = value; }
        }
        private NetAuthentication authenticationMessage;

        public NetAuthentication AuthenticationMessage
        {
            get { return authenticationMessage; }
            set { authenticationMessage = value; }
        }

        public NetAction(ActionType actionType)
        {
            this.actionType = actionType;
        }        
    }

    public class NetPublish
    {
        private string actionId;
        private NetAction.DestinationType destinationType;
        private string destination;
        private NetBrokerMessage message;
        
        private IDictionary<string, string> headers;

        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }
        
        public NetAction.DestinationType DestinationType
        {
            get { return destinationType; }
        }
        
        public string Destination
        {
            get { return destination; }
        }
        
        public NetBrokerMessage Message
        {
            get { return message; }
        }

        public IDictionary<string, string> Headers
        {
            get { return headers; }
            set { this.headers = value; }
        }

        public NetPublish(string destination, NetAction.DestinationType destinationType, NetBrokerMessage message)
        {
            this.destinationType = destinationType;
            this.destination = destination;
            this.message = message;
        }
    }

    public class NetBrokerMessage
    {
        private string messageId = "";
        private byte[] payload;
        private long expiration = -1;
        private long timestamp = -1;

        private IDictionary<string, string> headers;

        public string MessageId
        {
            get { return messageId; }
            set { messageId = value; }
        }

        public byte[] Payload
        {
            get { return payload; }
        }

        public long Expiration
        {
            get { return expiration; }
            set { expiration = value; }
        }

        public long Timestamp
        {
            get { return timestamp; }
            set { timestamp = value; }
        }

        /// <summary>
        /// Message headers
        /// </summary>
        public IDictionary<String, String> Headers
        {
            get { return headers; }
            set { this.headers = value; }
        }

        /// <summary>
        /// Set a message headers
        /// </summary>
        public void SetHeader(string header, string value)
        {
            if (headers == null)
            {
                Headers = new Dictionary<string, string>();
            }
            this.headers.Add(header, value);
        }

        public NetBrokerMessage(byte[] payload)
        {
            this.payload = payload;
        }
        public NetBrokerMessage(string payload)
        {
            this.payload = System.Text.Encoding.UTF8.GetBytes(payload);
        }
        /// <summary>
        /// Converts the binary content to a string using UTF8 encoding.
        /// </summary>
        /// <returns>A string representing the payload. </returns>
        public string GetPayloadAsString()
        {
            return System.Text.Encoding.UTF8.GetString(payload);
        }

    }
    public class NetPoll
    {
        private string actionId;

        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }
        private string destination;

        public string Destination
        {
            get { return destination; }
        }

        private long timeout;

        public long Timeout
        {
            get { return timeout; }
        }

        public NetPoll(string destination, long timeout)
        {
            this.destination = destination;
            this.timeout = timeout;
        }
    }

    public class NetAccepted
    {
        private string actionId;

        public string ActionId
        {
            get { return actionId; }
        }

        public NetAccepted(string actionId)
        {
            this.actionId = actionId;
        }
    }

    public class NetAcknowledge
    {
        private string actionId;

        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }
        private string messageId;

        public string MessageId
        {
            get { return messageId; }
        }
        private string destination;

        public string Destination
        {
            get { return destination; }
        }

        public NetAcknowledge(string destination, string messageId)
        {
            this.messageId = messageId;
            this.destination = destination;
        }
    }

    public class NetSubscribe
    {
        private string actionId;
        private string destination;
        private NetAction.DestinationType destinationType;

        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }

        public string Destination
        {
            get { return destination; }
        }

        public NetAction.DestinationType DestinationType
        {
            get { return destinationType; }
        }

        public NetSubscribe(string destination, NetAction.DestinationType destinationType)
        {
            this.destination = destination;
            this.destinationType = destinationType;
        }
    }

    public class NetUnsubscribe
    {
        private string actionId;
        
        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }
        private string destination;

        public string Destination
        {
            get { return destination; }
        }
        private NetAction.DestinationType destinationType;

        public NetAction.DestinationType DestinationType
        {
            get { return destinationType; }
        }

        public NetUnsubscribe(string destination, NetAction.DestinationType destinationType)
        {
            this.destination = destination;
            this.destinationType = destinationType;
        }
    }

    public class NetNotification
    {
        private string destination;
        private string subscription;
        private NetBrokerMessage message;

        private NetAction.DestinationType destinationType;

        private IDictionary<string, string> headers;

        public string Destination
        {
            get { return destination; }
        }

        public string Subscription
        {
            get { return subscription; }
        }

        public NetAction.DestinationType DestinationType
        {
            get { return destinationType; }
        }

        public NetBrokerMessage Message
        {
            get { return message; }
        }

        public IDictionary<string, string> Headers
        {
            get { return headers; }
        }

        internal NetNotification(string destination, NetAction.DestinationType destinationType, NetBrokerMessage message, string subscription, IDictionary<string, string> headers)
        {
            this.destination = destination;
            this.destinationType = destinationType;
            this.message = message;
            if (subscription == null)
                this.subscription = "";
            else
                this.subscription = subscription;
            this.headers = headers;
        }
    }

    public class NetFault
    {
        private string actionId;
        private string code;
        private string message;
        private string detail;

        private IDictionary<string, string> headers;

        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }

        public string Code
        {
            get { return code; }
        }

        public string Message
        {
            get { return message; }
        }

        public string Detail
        {
            get { return detail; }
            set { detail = value; }
        }

        public IDictionary<string, string> Headers
        {
            get { return headers; }
            set { headers = value; }
        }

        public static readonly NetMessage InvalidMessageSizeErrorMessage;
        public static readonly NetMessage UnknownEncodingProtocolMessage; // Not sent
        public static readonly NetMessage UnknownEncodingVersionMessage; // Not sent
        public static readonly NetMessage InvalidMessageFormatErrorMessage;
        public static readonly NetMessage UnexpectedMessageTypeErrorMessage;
        public static readonly NetMessage InvalidDestinationNameErrorMessage;
        public static readonly NetMessage InvalidMessageDestinationTypeErrorMessage;
        public static readonly NetMessage MaximumNrQueuesReachedMessage;
        public static readonly NetMessage MaximumDistinctSubscriptionsReachedMessage;
        public static readonly NetMessage PollTimeoutErrorMessage;
        public static readonly NetMessage NoMessageInQueueErrorMessage;
        public static readonly NetMessage AuthenticationFailedErrorMessage;
        public static readonly NetMessage UnknownAuthenticationTypeMessage;
        public static readonly NetMessage AccessDeniedErrorMessage;
        public static readonly NetMessage InvalidAuthenticationChannelType;

        static NetFault()
        {
            InvalidMessageSizeErrorMessage = buildNetFaultMessage("1101", "Invalid message size");
            UnknownEncodingProtocolMessage = buildNetFaultMessage("1102", "Unknown encoding protocol");
            UnknownEncodingVersionMessage = buildNetFaultMessage("1103", "Unknown encoding version");
            InvalidMessageFormatErrorMessage = buildNetFaultMessage("1201", "Invalid message format");
            UnexpectedMessageTypeErrorMessage = buildNetFaultMessage("1202", "Unexpected message type");
            InvalidDestinationNameErrorMessage = buildNetFaultMessage("2001", "Invalid destination name");
            InvalidMessageDestinationTypeErrorMessage = buildNetFaultMessage("2002", "Invalid destination type");
            MaximumNrQueuesReachedMessage = buildNetFaultMessage("2003", "Maximum number of queues reached");
            MaximumDistinctSubscriptionsReachedMessage = buildNetFaultMessage("2004", "Maximum distinct subscriptions reached");
            PollTimeoutErrorMessage = buildNetFaultMessage("2005", "Message poll timeout");
            NoMessageInQueueErrorMessage = buildNetFaultMessage("2006", "No message in local agent queue.");
            AuthenticationFailedErrorMessage = buildNetFaultMessage("3101", "Authentication failed");
            UnknownAuthenticationTypeMessage = buildNetFaultMessage("3102", "Unknown authentication type");
            InvalidAuthenticationChannelType = buildNetFaultMessage("3103", "Invalid authentication channel type");
            AccessDeniedErrorMessage = buildNetFaultMessage("3201", "Access denied");
        }

        public NetFault(string code, string message)
        {
            this.code = code;
            this.message = message;
        }

    

        public static NetMessage buildNetFaultMessage(string code, string message)
        {
            NetFault fault = new NetFault(code, message);
            NetAction action = new NetAction(NetAction.ActionType.FAULT);
            action.FaultMessage = fault;
            NetMessage msg = new NetMessage(action);
            return msg;
        }

        public static NetMessage getMessageFaultWithActionId(NetMessage message, string actionId)
        {
            NetFault fault = message.Action.FaultMessage;
            NetFault newFault = new NetFault(fault.Code, fault.Message);
            newFault.ActionId = actionId ;
            newFault.Detail = fault.Detail;

            NetAction action = new NetAction(NetAction.ActionType.FAULT);
            action.FaultMessage = newFault;

            return new NetMessage(action, message.Headers);
        }

        public static NetMessage getMessageFaultWithDetail(NetMessage message, string detail)
        {
            NetFault fault = message.Action.FaultMessage;
            NetFault newFault = new NetFault(fault.Code, fault.Message);
            newFault.ActionId = fault.ActionId;
            newFault.Detail = detail;

            NetAction action = new NetAction(NetAction.ActionType.FAULT);
            action.FaultMessage = newFault;

            return new NetMessage(action, message.Headers);
        }
    }

    public class NetPing
    {
        private string actionId;

        public string ActionId
        {
            get { return actionId; }
        }

        public NetPing(string actionId)
        {
            this.actionId = actionId;
        }
    }

    public class NetPong
    {
        private readonly static string universalActionId = "5E4FF374-B9AC-459b-B078-89A587D21001";

        public static string UniversalActionId
        {
            get { return NetPong.universalActionId; }
        }


        private string actionId;


        public string ActionId
        {
            get { return actionId; }
        }

        public NetPong(string actionId)
        {
            this.actionId = actionId;
        }
    }
    public class NetAuthentication
    {
        private string actionId;

        public string ActionId
        {
            get { return actionId; }
            set { actionId = value; }
        }
        private string authenticationType;

        public string AuthenticationType
        {
            get { return authenticationType; }
        }
        private byte[] token;

        public byte[] Token
        {
            get { return token; }
        }
        private string userId;

        public string UserId
        {
            get { return userId; }
            set { userId = value; }
        }
        private IList<string> roles;

        public IList<string> Roles
        {
            get { return roles; }
            set { roles = value; }
        }

        /**
         * Initializes a NetAuthentication instance.
         * @param token Can represent a password or some binary token. If the original value is text then it should be encoded in UTF-8.
         */
        public NetAuthentication(byte[] token, string authenticationType)
        {
            this.token = token;
            this.authenticationType = authenticationType;
        }
    }
}

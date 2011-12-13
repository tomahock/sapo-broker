
using System;
using System.Collections.Generic;

using SapoBrokerClient.Encoding.Thrift.Messages;
using Action = SapoBrokerClient.Encoding.Thrift.Messages.Action;

namespace SapoBrokerClient
{
	public class ThriftMessageConverter
	{
		#region Atom -> NetMessage
		
		public static NetMessage translate(Atom message)
		{
			NetAction action = getAction(message);
			IDictionary<string, string> headers = getHeaders(message);
			NetMessage netMessage = new NetMessage(action, headers);
			
			return netMessage;
		}
		
		private static IDictionary<string, string> getHeaders(Atom message)
		{
			return message.Header.Parameters;
		}
		
		private static NetAction getAction(Atom atom)
		{
			NetAction.ActionType actionType = translate(atom.Action.Action_type);
			NetAction action = new NetAction( actionType );
			
			switch(actionType)
			{
				case NetAction.ActionType.ACCEPTED:
					action.AcceptedMessage =getAcceptedMessage(atom) ;
					break;
				case NetAction.ActionType.NOTIFICATION:
					action.NotificationMessage = getNotificationMessage(atom) ;
					break;
				case NetAction.ActionType.FAULT:
					action.FaultMessage = getFaultMessage(atom) ;
					break;				
				case NetAction.ActionType.PONG:
					action.PongMessage = getPongMessage(atom);
					break;
				default:
					throw new Exception("Unexpected ActionType while unmarshalling message " + actionType.ToString() );
			}
			
			return action;
		}
		
		private static NetAccepted getAcceptedMessage(Atom atom)
		{
			NetAccepted accept = new NetAccepted(atom.Action.Accepted.Action_id);
			return accept;
		}
		
		private static NetNotification getNotificationMessage(Atom atom)
		{
			NetNotification notification = new NetNotification(atom.Action.Notification.Destination, translate(atom.Action.Notification.Destination_type), getBrokerMessage( atom.Action.Notification.Message ), atom.Action.Notification.Subscription, atom.Header.Parameters);
			return notification;
		}
		
		private static NetBrokerMessage getBrokerMessage(BrokerMessage brokerMessage)
		{
			NetBrokerMessage netBrokerMessage = new NetBrokerMessage(brokerMessage.Payload);
			netBrokerMessage.Expiration = brokerMessage.Expiration;
			netBrokerMessage.MessageId = brokerMessage.Message_id;
			netBrokerMessage.Timestamp = brokerMessage.Timestamp;
			return netBrokerMessage;
		}
		
		
		private static NetFault getFaultMessage(Atom atom)
		{
			NetFault fault = new NetFault(atom.Action.Fault.Fault_code, atom.Action.Fault.Fault_message);
			fault.ActionId = atom.Action.Fault.Action_id;
			fault.Detail = atom.Action.Fault.Fault_detail;
			
			return fault;
		}
		
		private static NetPong getPongMessage(Atom atom)
		{
			NetPong pong = new NetPong(atom.Action.Pong.Action_id);
			return pong;
		}
		
		private static NetAction.DestinationType translate( DestinationType destinationType)
		{
			switch(destinationType)
			{
				case DestinationType.QUEUE: return NetAction.DestinationType.QUEUE;
				case DestinationType.TOPIC: return NetAction.DestinationType.TOPIC;
				case DestinationType.VIRTUAL_QUEUE: return NetAction.DestinationType.VIRTUAL_QUEUE;
			}
			throw new Exception("Unexpected DestinationType while unmarshalling message " + destinationType.ToString() );
		}		
			
		private static NetAction.ActionType translate( ActionType actionType)
		{
			switch(actionType)
			{
				case ActionType.ACCEPTED: return NetAction.ActionType.ACCEPTED;
				case ActionType.NOTIFICATION: return NetAction.ActionType.NOTIFICATION;
				case ActionType.FAULT: return NetAction.ActionType.FAULT;
				case ActionType.PONG: return NetAction.ActionType.PONG;
			}
			throw new Exception("Unexpected ActionType while unmarshalling message " + actionType.ToString() );
		}
		
		
		#endregion
		
		#region NetMessage -> Atom
		
		public static Atom translate(NetMessage message)
		{
			Atom atom = new Atom();

            Action action = getAction(message);
            Header header = getHeaders(message);
						
			atom.Action = action;
			atom.Header = header;			
			
			return atom;
		}
		
		private static Header getHeaders(NetMessage message)
		{
			Header headers = new Header();
			headers.Parameters = new Dictionary<string, string>(message.Headers);
			return headers;
		}
		
		private static Action getAction(NetMessage message)
		{
			Action action = new Action();
			ActionType actionType = translate( message.Action.Action );
			action.Action_type = actionType;
			
			switch(actionType)
			{
				case ActionType.PUBLISH: 
					action.Publish = getPublishMessage(message);
					break;
				case ActionType.POLL: 
					action.Poll = getPollMessage(message);
					break;
				case ActionType.ACKNOWLEDGE:
					action.Ack_message = getAcknowledgeMessage(message);	
					break;
				case ActionType.SUBSCRIBE:
					action.Subscribe = getSubscribeMessage(message);
					break;
				case ActionType.UNSUBSCRIBE: 
					action.Unsubscribe = getUnsubscribeMessage(message);
					break;
				case ActionType.PING: 
					action.Ping = getPingMessage(message);
					break;
				case ActionType.AUTH:
					action.Auth = getAuthMessage(message);
					break;
				default:
					throw new Exception("Unexpected ActionType while marshalling message " + actionType.ToString() );
			}
			
			
			return action;
		}
		
		private static Publish getPublishMessage(NetMessage message)
		{
			NetPublish netPublish = message.Action.PublishMessage;
			
			Publish publish = new Publish();
			publish.Action_id = netPublish.ActionId;
			publish.Destination =  netPublish.Destination;
			publish.Destination_type = translate( netPublish.DestinationType );
			publish.Message = getBrokerMessage( netPublish.Message );
			
			return publish;
		}
		
		private static BrokerMessage getBrokerMessage(NetBrokerMessage brokerMessage)
		{
			BrokerMessage brokerMsg = new BrokerMessage();
			brokerMsg.Expiration = brokerMessage.Expiration;
			brokerMsg.Message_id = brokerMessage.MessageId;
			brokerMsg.Payload = brokerMessage.Payload;
			brokerMsg.Timestamp = brokerMessage.Timestamp;
			
			return brokerMsg;
		}
		
		private static Poll getPollMessage(NetMessage message)
		{
			NetPoll netPoll = message.Action.PollMessage;
			
			Poll poll = new Poll();
			poll.Action_id = netPoll.ActionId;
			poll.Destination = netPoll.Destination;
            poll.Timeout = netPoll.Timeout;

			return poll;
		}
		
		private static Acknowledge getAcknowledgeMessage(NetMessage message)
		{
			NetAcknowledge netAck = message.Action.AcknowledgeMessage;
			
			Acknowledge ack = new Acknowledge();
			ack.Action_id = netAck.ActionId;
            ack.Destination = netAck.Destination;
			ack.Message_id = netAck.MessageId;
			
			return ack;
		}
		
		private static Subscribe getSubscribeMessage(NetMessage message)
		{
			NetSubscribe netSubs = message.Action.SubscribeMessage;
			
			Subscribe subs = new Subscribe();
			subs.Action_id = netSubs.ActionId;
			subs.Destination = netSubs.Destination;
			subs.Destination_type = translate(netSubs.DestinationType);
			
			return subs;
		}
		
		
		private static Unsubscribe getUnsubscribeMessage(NetMessage message)
		{
			NetUnsubscribe netUnsub = message.Action.UnsbuscribeMessage;
			
			Unsubscribe unsubscribe = new Unsubscribe();
			unsubscribe.Action_id = netUnsub.ActionId;
			unsubscribe.Destination = netUnsub.Destination;
			unsubscribe.Destination_type = translate ( netUnsub.DestinationType );
			
			return unsubscribe;
		}
		
		private static Ping getPingMessage(NetMessage message)
		{	
			NetPing netPing = message.Action.PingMessage;
			
			Ping ping = new Ping();
			ping.Action_id = netPing.ActionId;
			
			return ping;
		}

        private static SapoBrokerClient.Encoding.Thrift.Messages.Authentication getAuthMessage(NetMessage message)
		{
			NetAuthentication  netAuth = message.Action.AuthenticationMessage;

            SapoBrokerClient.Encoding.Thrift.Messages.Authentication auth = new SapoBrokerClient.Encoding.Thrift.Messages.Authentication();
			auth.Action_id = netAuth.ActionId;
			auth.Authentication_type = netAuth.AuthenticationType;
            if (netAuth.Roles != null)
            {
                List<string> roles = netAuth.Roles as List<string>;
                if (roles == null)
                {
                    roles = new List<string>(netAuth.Roles);
                }
                auth.Roles = roles;
            }
			auth.Token = netAuth.Token;
			auth.User_id = netAuth.UserId;

			return auth;
		}
			
		
		private static DestinationType translate(NetAction.DestinationType destination)
		{
			switch(destination)
			{
				case NetAction.DestinationType.QUEUE: return DestinationType.QUEUE;
				case NetAction.DestinationType.TOPIC: return DestinationType.TOPIC;
				case NetAction.DestinationType.VIRTUAL_QUEUE: return DestinationType.VIRTUAL_QUEUE;
			}
			throw new Exception("Unexpected DestinationType while marshalling message " + destination.ToString() );
		}
		
		private static ActionType translate( NetAction.ActionType actionType)
		{
			switch(actionType)
			{
				case NetAction.ActionType.PUBLISH: return ActionType.PUBLISH;
				case NetAction.ActionType.POLL: return ActionType.POLL;
				case NetAction.ActionType.ACKNOWLEDGE: return ActionType.ACKNOWLEDGE;
				case NetAction.ActionType.SUBSCRIBE: return ActionType.SUBSCRIBE;
				case NetAction.ActionType.UNSUBSCRIBE: return ActionType.UNSUBSCRIBE;
				case NetAction.ActionType.PING: return ActionType.PING;
				case NetAction.ActionType.AUTH: return ActionType.AUTH;
			}
			throw new Exception("Unexpected ActionType while marshalling message " + actionType.ToString() );
		}
		
		#endregion
		
		
	}
}

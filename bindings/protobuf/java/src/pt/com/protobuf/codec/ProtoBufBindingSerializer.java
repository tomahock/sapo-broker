package pt.com.protobuf.codec;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.protobuf.codec.PBMessage.Atom;
import pt.com.protobuf.codec.PBMessage.Atom.Accepted;
import pt.com.protobuf.codec.PBMessage.Atom.AcknowledgeMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Action;
import pt.com.protobuf.codec.PBMessage.Atom.Authentication;
import pt.com.protobuf.codec.PBMessage.Atom.BrokerMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Fault;
import pt.com.protobuf.codec.PBMessage.Atom.Header;
import pt.com.protobuf.codec.PBMessage.Atom.Notification;
import pt.com.protobuf.codec.PBMessage.Atom.Parameter;
import pt.com.protobuf.codec.PBMessage.Atom.Ping;
import pt.com.protobuf.codec.PBMessage.Atom.Poll;
import pt.com.protobuf.codec.PBMessage.Atom.Pong;
import pt.com.protobuf.codec.PBMessage.Atom.Publish;
import pt.com.protobuf.codec.PBMessage.Atom.Subscribe;
import pt.com.protobuf.codec.PBMessage.Atom.Unsubscribe;
import pt.com.protobuf.codec.PBMessage.Atom.Authentication.AuthMessageType;
import pt.com.protobuf.codec.PBMessage.Atom.Authentication.ClientAuth;
import pt.com.protobuf.codec.PBMessage.Atom.Authentication.ClientChallengeResponse;
import pt.com.protobuf.codec.PBMessage.Atom.Authentication.ServerChallenge;
import pt.com.protobuf.codec.PBMessage.Atom.Authentication.ServerChallengeResponseClientChallenge;
import pt.com.types.BindingSerializer;
import pt.com.types.NetAccepted;
import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetPing;
import pt.com.types.NetPoll;
import pt.com.types.NetPong;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.NetAction.DestinationType;
import pt.com.types.NetAuthentication.AuthClientAuthentication;
import pt.com.types.NetAuthentication.AuthClientChallengeResponse;
import pt.com.types.NetAuthentication.AuthServerChallenge;
import pt.com.types.NetAuthentication.AuthServerChallengeResponseClientChallenge;

import com.google.protobuf.ByteString;

public class ProtoBufBindingSerializer implements BindingSerializer
{

	private static final Logger log = LoggerFactory.getLogger(ProtoBufBindingSerializer.class);

	@Override
	public NetMessage unmarshal(byte[] packet)
	{
		NetMessage message = null;
		try
		{
			PBMessage.Atom atom = PBMessage.Atom.parseFrom(packet);
			message = constructMessage(atom);
		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
		return message;
	}

	@Override
	public byte[] marshal(NetMessage message)
	{
		byte[] result = null;

		try
		{
			Header header = getHeaders(message);

			PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(message));
			if (header != null)
				atomBuilder.setHeader(header);

			Atom build = atomBuilder.build();
			result = build.toByteArray();
		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
		return result;
	}

	@Override
	public void marshal(NetMessage message, OutputStream out)
	{
		try
		{
			Header header = getHeaders(message);

			PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(message));
			if (header != null)
				atomBuilder.setHeader(header);

			atomBuilder.build().writeTo(out);

		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
	}

	private Action getAction(NetMessage netMessage)
	{
		PBMessage.Atom.Action.Builder builder = PBMessage.Atom.Action.newBuilder();

		switch (netMessage.getAction().getActionType())
		{
		case ACCEPTED:
			builder.setActionType(PBMessage.Atom.Action.ActionType.ACCEPTED);
			builder.setAccepted(getAccepted(netMessage));
			break;
		case ACKNOWLEDGE_MESSAGE:
			builder.setActionType(PBMessage.Atom.Action.ActionType.ACKNOWLEDGE_MESSAGE);
			builder.setAckMessage(getAcknowledge(netMessage));
			break;
		case FAULT:
			builder.setActionType(PBMessage.Atom.Action.ActionType.FAULT);
			builder.setFault(getFault(netMessage));
			break;
		case NOTIFICATION:
			builder.setActionType(PBMessage.Atom.Action.ActionType.NOTIFICATION);
			builder.setNotification(getNotification(netMessage));
			break;
		case POLL:
			builder.setActionType(PBMessage.Atom.Action.ActionType.POLL);
			builder.setPoll(getPool(netMessage));
			break;
		case PUBLISH:
			builder.setActionType(PBMessage.Atom.Action.ActionType.PUBLISH);
			builder.setPublish(getPublish(netMessage));
			break;
		case SUBSCRIBE:
			builder.setActionType(PBMessage.Atom.Action.ActionType.SUBSCRIBE);
			builder.setSubscribe(getSubscribe(netMessage));
			break;
		case UNSUBSCRIBE:
			builder.setActionType(PBMessage.Atom.Action.ActionType.UNSUBSCRIBE);
			builder.setUnsubscribe(getUnsubscribe(netMessage));
			break;
		case PING:
			builder.setActionType(PBMessage.Atom.Action.ActionType.PING);
			builder.setPing(getPing(netMessage));
			break;
		case PONG:
			builder.setActionType(PBMessage.Atom.Action.ActionType.PONG);
			builder.setPong(getPong(netMessage));
			break;
		case AUTH:
			builder.setActionType(PBMessage.Atom.Action.ActionType.AUTH);
			builder.setAuth(getAuth(netMessage));
		}
		return builder.build();
	}

	private Ping getPing(NetMessage netMessage)
	{
		NetPing gcsPing = netMessage.getAction().getPingMessage();

		PBMessage.Atom.Ping.Builder builder = PBMessage.Atom.Ping.newBuilder();
		builder.setActionId(gcsPing.getActionId());

		return builder.build();
	}

	private Pong getPong(NetMessage netMessage)
	{
		NetPong gcsPong = netMessage.getAction().getPongMessage();

		PBMessage.Atom.Pong.Builder builder = PBMessage.Atom.Pong.newBuilder();
		builder.setActionId(gcsPong.getActionId());

		return builder.build();
	}

	private Accepted getAccepted(NetMessage netMessage)
	{
		NetAccepted gcsAccepted = netMessage.getAction().getAcceptedMessage();

		PBMessage.Atom.Accepted.Builder builder = PBMessage.Atom.Accepted.newBuilder();
		builder.setActionId(gcsAccepted.getActionId());

		return builder.build();
	}

	private AcknowledgeMessage getAcknowledge(NetMessage netMessage)
	{
		NetAcknowledgeMessage net = netMessage.getAction().getAcknowledgeMessage();

		PBMessage.Atom.AcknowledgeMessage.Builder builder = PBMessage.Atom.AcknowledgeMessage.newBuilder();

		builder.setDestination(net.getDestination()).setMessageId(net.getMessageId());
		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Fault getFault(NetMessage netMessage)
	{
		NetFault net = netMessage.getAction().getFaultMessage();

		PBMessage.Atom.Fault.Builder builder = PBMessage.Atom.Fault.newBuilder();

		builder.setFaultCode(net.getCode()).setFaultMessage(net.getMessage());

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());
		if (net.getDetail() != null)
			builder.setFaultDetail(net.getDetail());

		return builder.build();
	}

	private PBMessage.Atom.Notification getNotification(NetMessage netMessage)
	{
		NetNotification net = netMessage.getAction().getNotificationMessage();

		String subs = net.getSubscription();
		if (subs == null)
			subs = "";

		PBMessage.Atom.Notification.Builder builder = PBMessage.Atom.Notification.newBuilder();
		builder.setDestination(net.getDestination()).setMessage(getMessageBroker(net.getMessage())).setDestinationType(translate(net.getDestinationType())).setSubscription(subs);

		return builder.build();
	}

	private Poll getPool(NetMessage netMessage)
	{
		NetPoll net = netMessage.getAction().getPollMessage();

		PBMessage.Atom.Poll.Builder builder = PBMessage.Atom.Poll.newBuilder();
		builder.setDestination(net.getDestination());

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Publish getPublish(NetMessage netMessage)
	{
		NetPublish net = netMessage.getAction().getPublishMessage();

		PBMessage.Atom.Publish.Builder builder = PBMessage.Atom.Publish.newBuilder();
		builder.setDestination(net.getDestination()).setMessage(getMessageBroker(net.getMessage())).setDestinationType(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Subscribe getSubscribe(NetMessage netMessage)
	{
		NetSubscribe net = netMessage.getAction().getSubscribeMessage();

		PBMessage.Atom.Subscribe.Builder builder = PBMessage.Atom.Subscribe.newBuilder();
		builder.setDestination(net.getDestination()).setDestinationType(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Unsubscribe getUnsubscribe(NetMessage netMessage)
	{
		NetUnsubscribe net = netMessage.getAction().getUnsbuscribeMessage();

		PBMessage.Atom.Unsubscribe.Builder builder = PBMessage.Atom.Unsubscribe.newBuilder();
		builder.setDestination(net.getDestination()).setDestinationType(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}
	
	private Authentication getAuth(NetMessage netMessage) {
		NetAuthentication auth = netMessage.getAction().getAuthorizationMessage();
		
		PBMessage.Atom.Authentication.Builder authBuilder = PBMessage.Atom.Authentication.newBuilder();
		authBuilder.setAuthMsgType(translate(auth.getAuthMessageType()));
		
		switch(auth.getAuthMessageType())
		{
		case CLIENT_ACKNOWLEDGE:
			{
				PBMessage.Atom.Authentication.ClientAcknowledge.Builder builder = PBMessage.Atom.Authentication.ClientAcknowledge.newBuilder();
				builder.setCommunicationId( auth.getAuthClientAcknowledge().getCommunicationId() );
				authBuilder.setClientAcknowledge(builder);
			}
			break;
		case CLIENT_AUTH:
			{
				PBMessage.Atom.Authentication.ClientAuth.Builder builder = PBMessage.Atom.Authentication.ClientAuth.newBuilder();
				AuthClientAuthentication authClientAuthrentication = auth.getAuthClientAuthentication();
				
				builder.setToken(ByteString.copyFrom(authClientAuthrentication.getToken())).
					setLocalCommunicationId(authClientAuthrentication.getLocalCommunicationId());
				if(authClientAuthrentication.getAuthenticationType() != null)
					builder.setAuthenticationType(authClientAuthrentication.getAuthenticationType());
				
				if(authClientAuthrentication.getUserId() != null)
					builder.setUserId(authClientAuthrentication.getUserId());
				
				if(authClientAuthrentication.getRoles() != null)
				{
					int i = 0;
					for(String role : authClientAuthrentication.getRoles())
						builder.setRole(i++, role);
				}
				authBuilder.setClientAuth(builder);
			}
			break;
		case CLIENT_CHALLENGE_RESPONSE:
			{
				PBMessage.Atom.Authentication.ClientChallengeResponse.Builder builder = PBMessage.Atom.Authentication.ClientChallengeResponse.newBuilder();
				AuthClientChallengeResponse challengeResp = auth.getAuthClientChallengeResponse();
				
				builder.setCommunicationId(challengeResp.getCommunicationId()).
						setChallenge(ByteString.copyFrom(challengeResp.getChallenge()));
								
				authBuilder.setClientChallengeResponse(builder);
			}
			break;
		case SERVER_CHALLENGE:
			{
				PBMessage.Atom.Authentication.ServerChallenge.Builder builder = PBMessage.Atom.Authentication.ServerChallenge.newBuilder();
				AuthServerChallenge authServerChallenge = auth.getAuthServerChallenge();
							
				builder.setCommunicationId(authServerChallenge.getCommunicationId()).
					setChallenge(ByteString.copyFrom(authServerChallenge.getChallenge())).
					setSecret(ByteString.copyFrom(authServerChallenge.getSecret())).
					setLocalCommunicationId(authServerChallenge.getLocalCommunicationId());
				
				if(authServerChallenge.getSecretType() != null)
					builder.setSecretType(authServerChallenge.getSecretType());
				
				authBuilder.setServerChallenge(builder);
			}
			break;
		case SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE:
			{
				PBMessage.Atom.Authentication.ServerChallengeResponseClientChallenge.Builder builder = PBMessage.Atom.Authentication.ServerChallengeResponseClientChallenge.newBuilder();
				AuthServerChallengeResponseClientChallenge authServerChallengeResponseClientChallenge = auth.getAuthServerChallengeResponseClientChallenge();
				
				builder.setCommunicationId(authServerChallengeResponseClientChallenge.getCommunicationId()).
						setProtectedChallenges(ByteString.copyFrom(authServerChallengeResponseClientChallenge.getProtectedChallenges()));
								
				authBuilder.setServerChallengeResponseClientChallenge(builder);
			}
			break;
		}
		
		
		return authBuilder.build();
	}

	private PBMessage.Atom.Header getHeaders(NetMessage netMessage)
	{
		PBMessage.Atom.Header.Builder builder = PBMessage.Atom.Header.newBuilder();
		boolean hasParams = false;

		Map<String, String> params = netMessage.getHeaders();
		if (params != null)
		{
			Iterator<String> it = params.keySet().iterator();
			while (it.hasNext())
			{
				hasParams = true;
				String k = it.next();
				String v = params.get(k);

				if ((k != null) && (v != null))
					builder.addParameter(PBMessage.Atom.Parameter.newBuilder().setName(k).setValue(v));
			}
		}
		if (hasParams)
			return builder.build();
		return null;
	}

	private BrokerMessage getMessageBroker(NetBrokerMessage message)
	{
		PBMessage.Atom.BrokerMessage.Builder builder = PBMessage.Atom.BrokerMessage.newBuilder();

		builder.setPayload(ByteString.copyFrom(message.getPayload()));

		if (message.getMessageId() != null)
			builder.setMessageId(message.getMessageId());

		if (message.getExpiration() != -1)
			builder.setExpiration(message.getExpiration());

		if (message.getTimestamp() != -1)
			builder.setTimestamp(message.getTimestamp());

		return builder.build();
	}

	private NetMessage constructMessage(PBMessage.Atom atom)
	{
		Map<String, String> parameters = null;
		if (atom.hasHeader())
		{
			parameters = extractParameters(atom.getHeader());
		}

		NetMessage message = new NetMessage(extractAction(atom.getAction()), parameters);
		return message;
	}

	private Map<String, String> extractParameters(Header header)
	{

		int paramsCount = header.getParameterCount();

		Map<String, String> parameters = new HashMap<String, String>();

		for (int i = 0; i != paramsCount; ++i)
		{
			Parameter param = header.getParameter(i);
			parameters.put(param.getName(), param.getValue());
		}

		return parameters;
	}

	private NetAction extractAction(Action action)
	{
		NetAction.ActionType actionType = translate(action.getActionType());
		NetAction netAction = new NetAction(actionType);

		switch (actionType)
		{
		case ACCEPTED:
			netAction.setAcceptedMessage(extractAcceptedMessage(action));
			break;
		case ACKNOWLEDGE_MESSAGE:
			netAction.setAcknowledgeMessage(extractAcknowledgeMessage(action));
			break;
		case FAULT:
			netAction.setFaultMessage(extractFaultMessage(action));
			break;
		case NOTIFICATION:
			netAction.setNotificationMessage(extractNotificationMessage(action));
			break;
		case POLL:
			netAction.setPollMessage(extractPoolMessage(action));
			break;
		case PUBLISH:
			netAction.setPublishMessage(extractPublishMessage(action));
			break;
		case SUBSCRIBE:
			netAction.setSubscribeMessage(extractSubscribeMessage(action));
			break;
		case UNSUBSCRIBE:
			netAction.setUnsbuscribeMessage(extractUnsubscribeMessage(action));
			break;
		case PING:
			netAction.setPingMessage(extractPingMessage(action));
			break;
		case PONG:
			netAction.setPongMessage(extractPongMessage(action));
			break;
		case AUTH:
			netAction.setAuthenticationMessage(extractAuthenticationMessage(action));
			break;
		}
		return netAction;
	}

	private NetBrokerMessage obtainBrokerMessage(BrokerMessage message)
	{

		NetBrokerMessage brkMsg = new NetBrokerMessage(message.getPayload().toByteArray());

		if (message.hasTimestamp())
			brkMsg.setTimestamp(message.getTimestamp());
		if (message.hasExpiration())
			brkMsg.setExpiration(message.getExpiration());
		if (message.hasMessageId())
			brkMsg.setMessageId(message.getMessageId());

		return brkMsg;
	}
	
	private PBMessage.Atom.DestinationType translate(DestinationType destinationType)
	{
		switch (destinationType)
		{
		case QUEUE:
			return PBMessage.Atom.DestinationType.QUEUE;
		case TOPIC:
			return PBMessage.Atom.DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return PBMessage.Atom.DestinationType.VIRTUAL_QUEUE;
		}
		// TODO: Throw checked exception
		return PBMessage.Atom.DestinationType.TOPIC;
	}
	
	static private NetAction.DestinationType translate(PBMessage.Atom.DestinationType destinationType)
	{
		switch (destinationType)
		{
		case QUEUE:
			return NetAction.DestinationType.QUEUE;
		case TOPIC:
			return NetAction.DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return NetAction.DestinationType.VIRTUAL_QUEUE;
		}
		// TODO: Throw checked exception
		return null;
	}
	
	private AuthMessageType translate(pt.com.types.NetAuthentication.AuthMessageType authMessageType) {
		switch(authMessageType)
		{
		case CLIENT_ACKNOWLEDGE:
			return AuthMessageType.CLIENT_ACKNOWLEDGE;
		case CLIENT_AUTH:
			return AuthMessageType.CLIENT_AUTH;
		case CLIENT_CHALLENGE_RESPONSE:
			return AuthMessageType.CLIENT_CHALLENGE_RESPONSE;
		case SERVER_CHALLENGE:
			return AuthMessageType.SERVER_CHALLENGE;
		case SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE:
			return AuthMessageType.SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE;
		}
		return null;
	}
	
	static private NetAuthentication.AuthMessageType translate(PBMessage.Atom.Authentication.AuthMessageType authMsgType)
	{
		switch(authMsgType)
		{
		case CLIENT_ACKNOWLEDGE:
			return NetAuthentication.AuthMessageType.CLIENT_ACKNOWLEDGE;
		case CLIENT_AUTH:
			return NetAuthentication.AuthMessageType.CLIENT_AUTH;
		case CLIENT_CHALLENGE_RESPONSE:
			return NetAuthentication.AuthMessageType.CLIENT_CHALLENGE_RESPONSE;
		case SERVER_CHALLENGE:
			return NetAuthentication.AuthMessageType.SERVER_CHALLENGE;
		case SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE:
			return NetAuthentication.AuthMessageType.SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE;
		}
		// TODO: Throw checked exception
		return null;
	}

	static private NetAction.ActionType translate(PBMessage.Atom.Action.ActionType actionType)
	{
		switch (actionType)
		{
		case ACCEPTED:
			return NetAction.ActionType.ACCEPTED;
		case ACKNOWLEDGE_MESSAGE:
			return NetAction.ActionType.ACKNOWLEDGE_MESSAGE;
		case FAULT:
			return NetAction.ActionType.FAULT;
		case NOTIFICATION:
			return NetAction.ActionType.NOTIFICATION;
		case POLL:
			return NetAction.ActionType.POLL;
		case PUBLISH:
			return NetAction.ActionType.PUBLISH;
		case SUBSCRIBE:
			return NetAction.ActionType.SUBSCRIBE;
		case UNSUBSCRIBE:
			return NetAction.ActionType.UNSUBSCRIBE;
		case PING:
			return NetAction.ActionType.PING;
		case PONG:
			return NetAction.ActionType.PONG;
		case AUTH:
			return NetAction.ActionType.AUTH;
			
		}
		// TODO: Throw checked exception
		return NetAction.ActionType.ACCEPTED;
	}

	private NetAccepted extractAcceptedMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		NetAccepted netAccepted = new NetAccepted(action.getAccepted().getActionId());

		return netAccepted;
	}

	private NetAcknowledgeMessage extractAcknowledgeMessage(Action action)
	{
		AcknowledgeMessage protoBufAckMsg = action.getAckMessage();
		// TODO: Verify if it's valid. Throw check exception if not
		String destination = protoBufAckMsg.getDestination();
		String messageId = protoBufAckMsg.getMessageId();
		NetAcknowledgeMessage ackMessage = new NetAcknowledgeMessage(destination, messageId);
		if (action.getAckMessage().hasActionId())
			ackMessage.setActionId(action.getAckMessage().getActionId());

		return ackMessage;
	}

	private NetFault extractFaultMessage(Action action)
	{
		Fault fault = action.getFault();
		// TODO: Verify if it's valid. Throw check exception if not
		String code = fault.getFaultCode();
		String message = fault.getFaultMessage();

		NetFault netFault = new NetFault(code, message);

		if (fault.hasActionId())
			netFault.setActionId(fault.getActionId());

		if (fault.hasFaultDetail())
			netFault.setDetail(fault.getFaultCode());

		return netFault;
	}

	private NetNotification extractNotificationMessage(Action action)
	{
		Notification notification = action.getNotification();
		// TODO: Verify if it's valid. Throw check exception if not

		String dest = notification.getDestination();
		NetAction.DestinationType destType = translate(notification.getDestinationType());
		NetBrokerMessage brkMsg = obtainBrokerMessage(notification.getMessage());
		String subs = notification.getSubscription();

		NetNotification netNotification = new NetNotification(dest, destType, brkMsg, subs);

		return netNotification;
	}

	private NetPoll extractPoolMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Poll poll = action.getPoll();
		String destination = poll.getDestination();

		NetPoll pollMsg = new NetPoll(destination);

		if (poll.hasActionId())
			pollMsg.setActionId(poll.getActionId());

		return pollMsg;
	}

	private NetPublish extractPublishMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Publish pub = action.getPublish();

		String dest = pub.getDestination();
		NetAction.DestinationType destType = translate(pub.getDestinationType());
		NetBrokerMessage brkMsg = obtainBrokerMessage(pub.getMessage());

		NetPublish netPub = new NetPublish(dest, destType, brkMsg);

		if (pub.hasActionId())
			netPub.setActionId(pub.getActionId());

		return netPub;
	}

	private NetSubscribe extractSubscribeMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Subscribe subs = action.getSubscribe();

		String dest = subs.getDestination();
		NetAction.DestinationType destType = translate(subs.getDestinationType());

		NetSubscribe netSubs = new NetSubscribe(dest, destType);

		if (subs.hasActionId())
			netSubs.setActionId(subs.getActionId());

		return netSubs;
	}

	private NetUnsubscribe extractUnsubscribeMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Unsubscribe unsubs = action.getUnsubscribe();

		String dest = unsubs.getDestination();
		NetAction.DestinationType destType = translate(unsubs.getDestinationType());

		NetUnsubscribe cgsUnsubs = new NetUnsubscribe(dest, destType);

		if (unsubs.hasActionId())
			cgsUnsubs.setActionId(unsubs.getActionId());

		return cgsUnsubs;
	}
	
	private NetPing extractPingMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Atom.Ping ping = action.getPing();

		NetPing netPing = new NetPing(ping.getActionId());

		return netPing;
	}

	private NetPong extractPongMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Atom.Pong pong = action.getPong();

		NetPong netPong = new NetPong(pong.getActionId());

		return netPong;
	}

	private NetAuthentication extractAuthenticationMessage(Action action) {
		// TODO: Verify if it's valid. Throw check exception if not
		Atom.Authentication auth = action.getAuth();
		
		NetAuthentication netAuth = new NetAuthentication(translate(auth.getAuthMsgType()));
				
		switch(auth.getAuthMsgType())
		{
			case CLIENT_ACKNOWLEDGE:
				String comId= auth.getClientAcknowledge().getCommunicationId();
				
				NetAuthentication.AuthClientAcknowledge clientAck = new NetAuthentication.AuthClientAcknowledge(comId);
				netAuth.setAuthClientAcknowledge(clientAck);
				break;
			case CLIENT_CHALLENGE_RESPONSE:
				ClientChallengeResponse clientChallengeResponse = auth.getClientChallengeResponse();
				
				NetAuthentication.AuthClientChallengeResponse clientChallengeResp = new NetAuthentication.AuthClientChallengeResponse(clientChallengeResponse.getCommunicationId(), clientChallengeResponse.getChallenge().toByteArray());
				netAuth.setAuthClientChallengeResponse(clientChallengeResp);
				break;
			case CLIENT_AUTH:
				ClientAuth clientAuth = auth.getClientAuth();
				NetAuthentication.AuthClientAuthentication netClientAuth = new NetAuthentication.AuthClientAuthentication(clientAuth.getToken().toByteArray(), clientAuth.getLocalCommunicationId());
				if(clientAuth.hasAuthenticationType())
					netClientAuth.setAuthenticationType(clientAuth.getAuthenticationType());
				if(clientAuth.hasUserId())
					netClientAuth.setUserId(clientAuth.getUserId());
				if(clientAuth.getRoleCount() != 0)
					netClientAuth.setRoles(clientAuth.getRoleList());
				netAuth.setAuthClientAuthentication(netClientAuth);
				break;
			case SERVER_CHALLENGE:
				ServerChallenge serverChallenge = auth.getServerChallenge();
				NetAuthentication.AuthServerChallenge netServerChallenge = new NetAuthentication.AuthServerChallenge(serverChallenge.getChallenge().toByteArray(), serverChallenge.getSecret().toByteArray(), serverChallenge.getCommunicationId(), serverChallenge.getLocalCommunicationId());
				if(serverChallenge.hasSecretType())
					netServerChallenge.setSecretType(serverChallenge.getSecretType());
				netAuth.setAuthServerChallenge(netServerChallenge);
				break;
			case SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE:
				ServerChallengeResponseClientChallenge serverChallengeResponseClientChallenge = auth.getServerChallengeResponseClientChallenge();
				NetAuthentication.AuthServerChallengeResponseClientChallenge netASRCC = new NetAuthentication.AuthServerChallengeResponseClientChallenge(serverChallengeResponseClientChallenge.getCommunicationId(), serverChallengeResponseClientChallenge.getProtectedChallenges().toByteArray()); 
				netAuth.setAuthServerChallengeResponseClientChallenge(netASRCC);
				break;
		}
		return netAuth;
	}

}

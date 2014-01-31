package pt.com.broker.auth;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.channels.ChannelAttributes;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.global.Agents;
import pt.com.gcs.conf.global.Agents.Agent;
import pt.com.gcs.conf.global.Authorization;
import pt.com.gcs.conf.global.BrokerSecurityPolicy;
import pt.com.gcs.conf.global.ChannelType;
import pt.com.gcs.conf.global.Condition;
import pt.com.gcs.conf.global.Condition.Address;
import pt.com.gcs.conf.global.Policies;
import pt.com.gcs.conf.global.Policies.Policy;
import pt.com.gcs.conf.global.Policies.Policy.Acl.Entry;
import pt.com.gcs.messaging.DestinationMatcher;
import pt.com.gcs.messaging.GlobalConfigMonitor;
import pt.com.gcs.messaging.GlobalConfigMonitor.GlobalConfigModifiedListener;

/**
 * AccessControl class implements access control functionality, namely validation of users permission to perform intended operations such publishing or subscription.
 */
public class AccessControl
{
	private static Logger log = LoggerFactory.getLogger(AccessControl.class);

	/**
	 * Agent Access Control List
	 */
	private static List<AclEntry> agentAcl = new ArrayList<AclEntry>();

	public static class ValidationResult
	{
		public boolean accessGranted;
		public String reasonForRejection;
	}

	public enum Privilege
	{
		READ, WRITE;
		public static Privilege fromValue(pt.com.gcs.conf.global.Privilege priv)
		{
			switch (priv)
			{
			case READ:
				return READ;
			case WRITE:
				return WRITE;
			}
			return null;
		}
	}

	public enum Autorization
	{
		PERMIT, DENY;
		public static Autorization fromValue(pt.com.gcs.conf.global.Authorization auth)
		{
			switch (auth)
			{
			case PERMIT:
				return PERMIT;
			case DENY:
				return DENY;
			}
			return null;
		}
	}

	private static ValidationResult granted;
	private static ValidationResult refused_authRequired;

	private static boolean accessControlRequired = true;

	/*
	 * Access Control initialization code
	 */

	static
	{
		init();
	}

	private static void init()
	{
		granted = new ValidationResult();
		granted.accessGranted = true;

		refused_authRequired = new ValidationResult();
		refused_authRequired.accessGranted = false;
		refused_authRequired.reasonForRejection = "Authentication Required!";

		loadSecurityPolicies();

		// ADD IT HERE
		GlobalConfigMonitor.addGlobalConfigModifiedListener(new GlobalConfigModifiedListener()
		{

			@Override
			public void globalConfigModified()
			{
				synchronized (agentAcl)
				{
					loadSecurityPolicies();
				}
			}

		});
	}

	static private void loadSecurityPolicies()
	{
		BrokerSecurityPolicy securityPolicy = GcsInfo.getSecurityPolicies();
		if (securityPolicy == null)
		{
			accessControlRequired = false;
			return;
		}

		// Get Policies
		Policies secPolicies = securityPolicy.getPolicies();
		if (secPolicies == null)
		{
			accessControlRequired = false;
			return;
		}
		List<Policy> policies = secPolicies.getPolicy();

		if (policies == null)
		{
			accessControlRequired = false;
			return;
		}

		// There are security policies. Determine if there is one for "this"
		// agent.

		Agents agents = securityPolicy.getAgents();
		Agent agent = null;

		if (agents != null)
		{

			String agentName = GcsInfo.getAgentName();

			if (agents.getAgent() != null)
			{
				for (Agent tmpAgent : agents.getAgent())
				{
					if (agentName.equals(tmpAgent.getAgentName()))
					{
						agent = tmpAgent;
						break;
					}
				}
			}
		}

		if (agent != null)
		{
			String agentPolicyName = agent.getAgentPolicy().getPolicyName();
			addPolicy(policies, agentPolicyName);
		}
		else
		{
			// There is no agent specific policy. Load default
			addPolicy(policies, "default");
		}

		if (agentAcl.isEmpty())
			accessControlRequired = false;
	}

	private static void addPolicy(List<Policy> policies, String policyName)
	{
		for (Policy policy : policies)
		{
			if (policyName.equals(policy.getPolicyName()))
			{
				addPolicyEntries(policy.getAcl().getEntry());
				if (StringUtils.isNotBlank(policy.getInherits()))
				{
					addPolicy(policies, policy.getInherits());
				}
				break;
			}
		}

	}

	private static void addPolicyEntries(List<Entry> entries)
	{
		List<AclEntry> newAgentAcl = new ArrayList<AclEntry>();
		for (Entry entry : entries)
		{
			List<pt.com.gcs.conf.global.Privilege> privileges = entry.getPrivilege();
			String destination = entry.getDestination();
			List<pt.com.gcs.conf.global.DestinationType> destinationTypes = entry.getDestinationType();
			List<AclPredicate> predicates = translatePredicates(entry.getCondition());
			Authorization action = entry.getAction();

			for (pt.com.gcs.conf.global.Privilege priv : privileges)
			{
				for (pt.com.gcs.conf.global.DestinationType destType : destinationTypes)
				{
					newAgentAcl.add(new AclEntry(Autorization.fromValue(action), Privilege.fromValue(priv), destination, translateDestinationType(destType), predicates));
				}
			}
		}
		synchronized (agentAcl)
		{
			agentAcl.clear();
			agentAcl.addAll(newAgentAcl);
		}
	}

	private static DestinationType translateDestinationType(pt.com.gcs.conf.global.DestinationType destType)
	{
		switch (destType)
		{
		case QUEUE:
			return DestinationType.QUEUE;
		case TOPIC:
			return DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return DestinationType.VIRTUAL_QUEUE;
		}
		return null;
	}

	private static List<AclPredicate> translatePredicates(List<Condition> conditions)
	{
		List<AclPredicate> aclPreds = new ArrayList<AclPredicate>(conditions.size());

		for (Condition cond : conditions)
		{

			switch (cond.getConditionType())
			{
			case ADDRESS:
				Address address = cond.getAddress();
				try
				{
					int mask = address.getMask() == null ? 32 : (int) address.getMask();
					aclPreds.add(new AddressPredicate(InetAddress.getByName(address.getValue()), mask));
				}
				catch (Throwable t)
				{
					log.error("Invalid Address or mask", "Address: " + address.getValue() + "Value :" + address.getMask());
				}
				break;
			case CHANNELTYPE:
				ChannelType channelType = cond.getChannelType();
				aclPreds.add(new ChannelTypePredicate(channelType));
				break;
			case ROLE:
				String role = cond.getRole();
				aclPreds.add(new RolePredicate(role));
				break;
			case ALWAYS:
				aclPreds.add(AlwaysPredicate.getInstance());
				break;
			case AND:
				List<Condition> andConditions = cond.getCondition();
				List<AclPredicate> andPredicates = translatePredicates(andConditions);
				aclPreds.add(new AndPredicate(andPredicates));
				break;
			}
		}

		return aclPreds;
	}

	/*
	 * End of access control initialization code
	 */

	public static synchronized SessionAcl getSessionAcl(SessionProperties sessionProperties)
	{
		SessionAcl sessionAcl = new SessionAcl();

		synchronized (agentAcl)
		{
			for (AclEntry entry : agentAcl)
			{
				for (AclPredicate pred : entry.getConditions())
				{
					if (pred.match(sessionProperties))
					{
						sessionAcl.add(entry);
						break;
					}
				}
			}
		}

		return sessionAcl;
	}

	public static ValidationResult validate(NetMessage message, Session session)
	{
		if (!accessControlRequired)
		{
			return granted;
		}

		Privilege priv = null;
		DestinationType dest = null;
		String destinationName = null;

		switch (message.getAction().getActionType())
		{
		case POLL:
			NetPoll poll = message.getAction().getPollMessage();
			dest = DestinationType.QUEUE;
			destinationName = poll.getDestination();
			priv = Privilege.READ;
			break;
		case PUBLISH:
			NetPublish pub = message.getAction().getPublishMessage();
			dest = pub.getDestinationType();
			destinationName = pub.getDestination();
			priv = Privilege.WRITE;
			break;
		case SUBSCRIBE:
			NetSubscribe subs = message.getAction().getSubscribeMessage();
			if (subs.getDestinationType().equals(NetAction.DestinationType.VIRTUAL_QUEUE))
			{
				dest = NetAction.DestinationType.TOPIC;
				destinationName = StringUtils.substringAfter(subs.getDestination(), "@");
			}
			else
			{
				dest = subs.getDestinationType();
				destinationName = subs.getDestination();
			}
			priv = Privilege.READ;
			break;
		default:
			return granted;
		}

		return validate(dest, destinationName, priv, session);
	}

	public static ValidationResult validate(DestinationType destinationType, String destinationName, Privilege privilege, Session session)
	{
		if (!accessControlRequired)
		{
			return granted;
		}

		SessionAcl sessionAcl;

		if (privilege.equals(Privilege.READ))
			sessionAcl = session.getReadSessionAcl();
		else
			sessionAcl = session.getWriteSessionAcl();

		if (sessionAcl.isEmpty())
			return granted;

		SessionProperties sessionProperties = null;
		if (session != null)
			sessionProperties = session.getSessionProperties();

		for (AclEntry entry : sessionAcl)
		{
			if (destinationType.equals(entry.getDestinationType()))
			{
				if (match(entry.getDestination(), destinationName) /* || match(destinationName, entry.getDestination()) */)
				{
					for (AclPredicate pred : entry.getConditions())
					{
						if (session == null)
						{
							return refused_authRequired;
						}
						if (pred.match(sessionProperties))
						{
							if (entry.getAutorizationType().equals(Autorization.PERMIT))
								return granted;

							ValidationResult res = new ValidationResult();
							res.accessGranted = false;
							res.reasonForRejection = String.format("Access denied! Destination type: %s, Destination name: %s, Privilege: %s", destinationType, destinationName, privilege);
							return res;
						}
					}
				}
			}
		}

		return granted;
	}

	public static boolean deliveryAllowed(NetMessage response, DestinationType destinationType, Channel channel, String subscription, String destination)
	{
		Object _session = ChannelAttributes.get(ChannelAttributes.getChannelId(channel), "BROKER_SESSION_PROPERTIES");
		Session sessionProps = null;
		if (_session == null)
		{
			_session = new Session();
		}
		if (_session != null)
		{
			sessionProps = (Session) _session;
		}

		ValidationResult validationResult = validate(destinationType, destination, Privilege.READ, sessionProps);
		if (!validationResult.accessGranted)
		{
			log.info(String.format("Message delivery refused to '%s'. Subscription: '%s', Destination: '%s'", channel.toString(), subscription, destination));
		}

		return validationResult.accessGranted;
	}

	private static boolean match(String destinationName, String destination)
	{
		boolean result = DestinationMatcher.match(destinationName, destination);
		return result;
	}
}

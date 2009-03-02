package pt.com.gcs.security;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.Agents;
import pt.com.gcs.conf.Authorization;
import pt.com.gcs.conf.BrokerSecurityPolicy;
import pt.com.gcs.conf.ChannelType;
import pt.com.gcs.conf.Condition;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.Policies;
import pt.com.gcs.conf.Agents.Agent;
import pt.com.gcs.conf.Condition.Address;
import pt.com.gcs.conf.Policies.Policy;
import pt.com.gcs.conf.Policies.Policy.Acl.Entry;
import pt.com.gcs.messaging.DestinationMatcher;
import pt.com.types.NetAction.DestinationType;

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
		READ,
		WRITE;
		public static Privilege fromValue(pt.com.gcs.conf.Privilege priv) {
	       switch(priv){
	       	case READ: return READ;
	       	case WRITE: return WRITE;
	       }
	       return null;
	    }
	}
	public enum Autorization
	{
		PERMIT,
		DENY;
		public static Autorization fromValue(pt.com.gcs.conf.Authorization auth) {
	       switch(auth){
	       	case PERMIT: return PERMIT;
	       	case DENY: return DENY;
	       }
	       return null;
	    }
	}
	
	private static ValidationResult granted;
	
	private static boolean accessControlRequired= true;
	
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
		
		BrokerSecurityPolicy securityPolicy = GcsInfo.getSecurityPolicy();
		if(securityPolicy== null)
		{
			accessControlRequired = false;
			return;
		}

		// Get Policies
		
		Policies secPolicies = securityPolicy.getPolicies();
		if(secPolicies  == null)
		{
			accessControlRequired = false;
			return;
		}
		List<Policy> policies = secPolicies .getPolicy();
		
		if(policies == null)
		{
			accessControlRequired = false;
			return;
		}
		
		// There are security policies. Determine if there is one for "this" agent.
		
		Agents agents = securityPolicy.getAgents();
		Agent agent = null;
				
		if(agents != null)
		{
			
			String agentName = GcsInfo.getAgentName();
			
			if(agents.getAgent() != null)
			{
				for(Agent tmpAgent: agents.getAgent())
				{
					if(agentName.equals(tmpAgent.getAgentName()))
					{
						agent = tmpAgent;
					}
				}
			}
		}
		
		if(agent != null)
		{
			String agentPolicyName = agent.getAgentPolicy().getPolicyName();
			addPolicy(policies, agentPolicyName);
		} else {
			// There is no agent specific policy. Load default
			addPolicy(policies, "default");
		}
		
		if( agentAcl.isEmpty() ) 
			accessControlRequired = false;
	}
	
	
	private static void addPolicy(List<Policy> policies, String policyName) {
		for(Policy policy : policies)
		{
			if(policyName.equals(policy.getPolicyName()))
			{
				addPolicyEntries(policy.getAcl().getEntry());
				if(StringUtils.isNotBlank(policy.getInherits()) )
				{
					addPolicy(policies, policy.getInherits());
				}
			}
			break;
		}
		
	}

	private static void addPolicyEntries(List<Entry> entries) {
		for(Entry entry : entries)
		{
			List<pt.com.gcs.conf.Privilege> privileges = entry.getPrivilege();			
			String destination = entry.getDestination();
			List<pt.com.gcs.conf.DestinationType> destinationTypes = entry.getDestinationType();
			List<AclPredicate> predicates = translatePredicates(entry.getCondition());
			Authorization action = entry.getAction();

			for(pt.com.gcs.conf.Privilege priv: privileges)
			{
				for(pt.com.gcs.conf.DestinationType destType : destinationTypes)
				{
					agentAcl.add(new AclEntry(Autorization.fromValue(action), Privilege.fromValue(priv), destination, translateDestinationType(destType), predicates));
				}
			}
		}
	}


	private static DestinationType translateDestinationType(pt.com.gcs.conf.DestinationType destType)
	{
		switch(destType){
			case QUEUE: return DestinationType.QUEUE;
			case TOPIC: return DestinationType.TOPIC;
			case TOPIC_AS_QUEUE: return DestinationType.VIRTUAL_QUEUE;
		}
		return null;
	}


	private static List<AclPredicate> translatePredicates(List<Condition> conditions) {
		List<AclPredicate> aclPreds = new ArrayList<AclPredicate>(conditions.size());
		
		for(Condition cond : conditions)
		{
			
			switch(cond.getConditionType())
			{
			case  ADDRESS:
				Address address = cond.getAddress();
				try{
					aclPreds.add( new AddressPredicate(InetAddress.getByName(address.getValue()), (int)address.getMask()));
				}catch(Throwable t)
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
				aclPreds.add( new RolePredicate( role ) );
				break;
			case AND:
				List<Condition> andConditions = cond.getCondition();
				List<AclPredicate> andPredicates =  translatePredicates(andConditions);
				aclPreds.add( new AndPredicate(andPredicates) );
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
		
		for(AclEntry entry : agentAcl)
		{
			for(AclPredicate pred : entry.getConditions() )
			{
				if( pred.match(sessionProperties) )
				{
					sessionAcl.add(entry);
					break;
				}
			}
		}
		
		return sessionAcl;		
	}
	
	
	public static ValidationResult validate(DestinationType destinationType, String destinationName, Privilege privilege, Session session)
	{
		if( !accessControlRequired )
		{
			return granted;
		}
		
		SessionAcl sessionAcl;
		
		if(privilege.equals(Privilege.READ) )
			sessionAcl = session.getReadSessionAcl();
		else
			sessionAcl = session.getWriteSessionAcl();
		
		if(sessionAcl.isEmpty())
			return granted;
		
		SessionProperties sessionProperties = session.getSessionProperties();
		
		for(AclEntry entry : sessionAcl)
		{
			if(destinationType.equals(entry.getDestinationType()))
				if( DestinationMatcher.match(destinationName, entry.getDestination())) 
					for(AclPredicate pred : entry.getConditions())
					{
						if(pred.match(sessionProperties))
						{
							if(entry.getAutorizationType().equals(Autorization.PERMIT))
								return granted;
							
							ValidationResult res = new ValidationResult();
							res.accessGranted = false;
							res.reasonForRejection = "Access denied! Reason: " + pred.toString();
							return res;							
						}
						
					}
					
		}
		
		return granted;
	}


	private static boolean match(String destinationName, String destination) {
		return true;
	}
	
}

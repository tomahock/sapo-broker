package pt.com.gcs.security;

import java.util.List;

import pt.com.types.NetAction;

public class AclEntry {
	private AccessControl.Autorization autorizationType;
	private NetAction.DestinationType destinationType;
	private AccessControl.Privilege privilege;
	private String destination;
	private List<AclPredicate> conditions;
	
	public AclEntry(AccessControl.Autorization autorizationType,  AccessControl.Privilege privilege, String destination, NetAction.DestinationType destinationType, List<AclPredicate> conditions)
	{
		this.autorizationType = autorizationType;
		this.privilege = privilege;
		this.destination = destination;
		this.destinationType = destinationType;
		this.conditions = conditions;
	}
	public AccessControl.Autorization getAutorizationType() {
		return autorizationType;
	}
	public AccessControl.Privilege getPrivilege() {
		return privilege;
	}
	public String getDestination() {
		return destination;
	}
	public NetAction.DestinationType getDestinationType(){
		return destinationType;
	}
	public List<AclPredicate> getConditions() {
		return conditions;
	}
}

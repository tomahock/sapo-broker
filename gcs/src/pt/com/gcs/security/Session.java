package pt.com.gcs.security;

import org.apache.mina.core.session.IoSession;

import pt.com.gcs.security.AccessControl.Privilege;

public class Session {
	private IoSession session;
	private SessionProperties sessionProperties;
	private SessionAcl readSessionAcl;
	private SessionAcl writeSessionAcl;
	
	public Session(IoSession session)
	{
		this(session, new SessionProperties());
	}
	
	public Session(IoSession session, SessionProperties sessionProperties)
	{
		this.session = session;
		this.sessionProperties = sessionProperties;
		upadateAcl();
	}
	
	public IoSession getSession() {
		return session;
	}
	public SessionProperties getSessionProperties() {
		return sessionProperties;
	}
	public SessionAcl getReadSessionAcl() {
		return readSessionAcl;
	}
	public SessionAcl getWriteSessionAcl() {
		return writeSessionAcl;
	}
	
	public void upadateAcl()
	{
		readSessionAcl = new SessionAcl();
		writeSessionAcl = new SessionAcl();
		SessionAcl fullSessionAcl = AccessControl.getSessionAcl(sessionProperties);
		
		for(AclEntry entry : fullSessionAcl)
		{
			if(entry.getPrivilege().equals(Privilege.READ))
			{
				readSessionAcl.add(entry);
			} else {
				writeSessionAcl.add(entry);
			}
		}
	}
}

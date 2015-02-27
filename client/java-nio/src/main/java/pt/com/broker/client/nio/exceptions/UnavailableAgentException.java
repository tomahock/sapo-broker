package pt.com.broker.client.nio.exceptions;

public class UnavailableAgentException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnavailableAgentException() {
		super("No agent available.");
	}

	public UnavailableAgentException(Throwable cause) {
//		super(cause);
		super("No agent available.", cause);
	}
	
	

}

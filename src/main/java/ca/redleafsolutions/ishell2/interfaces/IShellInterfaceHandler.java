package ca.redleafsolutions.ishell2.interfaces;

import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequest;

/**
 * The IShellInterfaceHandler is a hook for any implementation to become an inline handler of any request.
 */
public interface IShellInterfaceHandler {
	/** Check if the request is supposed to be handled by this instance
	 * @param request The requst
	 * @return true if this request should be handled by this instance. False otherwise.
	 */
	boolean isOwnRequest (IShellRequest request);
	
	/** Handle the request
	 * @param request The request
	 * @return the result of the request execution
	 * @throws IShellException when execution have failed and any exception was thrown out
	 */
	IShellObject execute (IShellRequest request) throws IShellException;
}

package ca.redleafsolutions.ishell2;

/** 
 * Implementors of this interface will automatically be passed the full iShell request object with all parameters, headers, cookies etc. 
 */
public interface IShellRequestConsumer {
	/**
	 * Pass the request to object
	 * @param request the full iShel request
	 */
	public void setRequest (IShellRequestSingle request);
}

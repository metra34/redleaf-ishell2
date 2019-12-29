package ca.redleafsolutions.ishell2;

import ca.redleafsolutions.ObjectMap;

public class IShellHTTPResponse<T> {
	private T o;
	private ObjectMap headers = new ObjectMap ();

	public IShellHTTPResponse (T o) {
		this.o = o;
	}
	
	public void setHeader (String key, String value) {
		this.headers.put (key, value);
	}

	public ObjectMap getHeaders () {
		return headers;
	}
	
	public T getObject () {
		return o;
	}
}

package ca.redleafsolutions.ishell2;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellRequestHTTP extends IShellRequestSingle {
	private ObjectMap requestHeaders;

	public IShellRequestHTTP(JSONItem json) throws JSONValidationException {
		super(json);
	}

	public IShellRequestHTTP (ParseRequestResults parsed) {
		super (parsed);
	}

	public void setRequestHeaders (ObjectMap headers) {
		this.requestHeaders = headers;
	}
	public ObjectMap getRequestHeaders () {
		return requestHeaders;
	}
}

package ca.redleafsolutions.ishell2;

import java.util.List;

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
		for (String key:requestHeaders.keySet ()) {
			List<String> list = (List<String>)requestHeaders.get (key);
			if (list.size () == 1) {
				Object value = list.get (0);
				requestHeaders.put (key, value);
			}
		}
	}
	public ObjectMap getRequestHeaders () {
		return requestHeaders;
	}
}

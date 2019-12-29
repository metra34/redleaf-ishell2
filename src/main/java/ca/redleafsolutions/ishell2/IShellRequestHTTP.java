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

	@SuppressWarnings ("rawtypes")
	public void setRequestHeaders (ObjectMap headers) {
		this.requestHeaders = new ObjectMap ();
		for (String key:headers.keySet ()) {
			Object o = headers.get (key);
			if (o instanceof List) {
				if (((List)o).size () == 1) {
					o = ((List)o).get (0);
				}
			}
			requestHeaders.put (key, o);
		}
	}
	
	public ObjectMap getRequestHeaders () {
		return requestHeaders;
	}
}

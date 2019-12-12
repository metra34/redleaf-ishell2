package ca.redleafsolutions.ishell2.api;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class VOIDAPIResponse implements JSONWritable {
	@Override
	public JSONItem toJSON () throws JSONValidationException {
		return JSONItem.newObject ();
	}
}

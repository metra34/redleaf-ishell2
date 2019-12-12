package ca.redleafsolutions;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class About implements JSONWritable{

	private ObjectMap products = new ObjectMap();
	
	public About (JSONItem json) throws JSONValidationException {
		products.fromJSON(json.getJSON("products"));
	}
	
	@Override
	public JSONItem toJSON() throws JSONValidationException {
		return products.toJSON();
	}

}

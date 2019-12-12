package ca.redleafsolutions.ishell2.utest;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONUtils;
import ca.redleafsolutions.json.JSONValidationException;

public class UTestExpectJSON implements UTestExpectedResult {
	private String filter;
	private Object expvalue;

	public UTestExpectJSON (JSONItem json, ObjectMap map) throws JSONValidationException {
		this.filter = TemplateUtils.evaluate (json.getString ("filter"), map);
		this.expvalue = json.get ("value");
	}

	@Override
	public boolean asExpected (Object result) {
		if (result == null)
			return false;
		if (result instanceof JSONItem) {
			try {
				Object retval = JSONUtils.get ((JSONItem)result, filter);
				return expvalue.equals (retval);
			} catch (JSONValidationException e) {
			}
		}
		return false;
	}
	
	@Override
	public String toString () {
		return expvalue + " (in " + filter + ")";
	}
}

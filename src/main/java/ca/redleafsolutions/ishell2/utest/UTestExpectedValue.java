package ca.redleafsolutions.ishell2.utest;

import ca.redleafsolutions.json.JSONItem;

public class UTestExpectedValue implements UTestExpectedResult {
	private String expected;

	public UTestExpectedValue (String exp) {
		this.expected = exp;
	}

	@Override
	public boolean asExpected (Object result) {
		if (result == null) 
			return false;
		
		if (result instanceof JSONItem)
			return false;
		return expected.equals (result);
	}

	@Override
	public String toString () {
		return expected;
	}
}

/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class Usage implements JSONWritable {

	public Usage () {
	}

	public IShellObject x (String cmdline) throws IShellException {
		ParseRequestResults parsed = new ParseRequestResults (cmdline);
		IShellRequest request = new IShellRequestSingle (parsed);
		IShellObject res = iShell.getInstance ().execute (request);
//		res.showDetails ();
		return res;
	}
	
	@Override
	public String toString () {
		String s = super.toString ();		
		return s;
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		return json;
	}
}

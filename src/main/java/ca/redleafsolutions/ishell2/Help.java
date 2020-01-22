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

import java.util.Map;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class Help implements JSONWritable, HTMLWritable{
	private Map<String, Object> extensions;

	public Help (Map<String, Object> extensions) {
		this.extensions = extensions;
	}

	@Override
	public String toString () {
		String s = "";
		for (String key:extensions.keySet ()) {
			s += key + ": " + extensions.get (key).getClass ().getCanonicalName () + "\n";
		}
		return s;
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		for (String key:extensions.keySet ()) {
			json.put (key, extensions.get (key).getClass ().getCanonicalName ());
		}
		return json;
	}

	@Override
	public String toHTML() {
		StringBuffer s = new StringBuffer();
		s.append("<html><body>");
		for (String key:extensions.keySet ()) {
			s.append("<li>").append(key).append (": ").append (extensions.get (key).getClass ().getCanonicalName ());
		}
		return s.toString();
	}
}

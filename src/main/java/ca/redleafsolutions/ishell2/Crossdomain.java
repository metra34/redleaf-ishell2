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

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class Crossdomain implements JSONWritable, XMLWritable {
	private JSONItem json;

	public Crossdomain (JSONItem json) {
		this.json = json;
	}

	@Override
	public JSONItem toJSON () {
		return json;
	}

	@Override
	public Element toXML (Document doc) {
		Element rootElement = doc.createElement ("cross-domain-policy");
		doc.appendChild (rootElement);
		for (Iterator<?> keyiterator = json.keys (); keyiterator.hasNext ();) {
			String key = (String)keyiterator.next ();
			Element childElement = doc.createElement (key);
			rootElement.appendChild (childElement);
			JSONItem child;
			try {
				child = json.getJSON (key);
				for (Iterator<?> childiterator = child.keys (); childiterator.hasNext ();) {
					String childkey = (String)childiterator.next ();
					childElement.setAttribute (childkey, child.getString (childkey));
				}
			} catch (JSONValidationException e) {
			}
		}
		return rootElement;
	}
}

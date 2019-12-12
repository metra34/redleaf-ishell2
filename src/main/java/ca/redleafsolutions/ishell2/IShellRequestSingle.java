/*
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadable;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellRequestSingle extends IShellRequest implements JSONReadable {
	private List<String> path;
	private ObjectMap params;
	private boolean details;
	private ParseRequestResults parsed;

	public IShellRequestSingle (ParseRequestResults parsed) {
		this.parsed = parsed;
		this.path = parsed.getPath ();
		this.params = parsed.getParams ();

		this.details = false;
		this.timing = false;
		if ((path != null) && (path.size () > 1)) {
			if ("details".equalsIgnoreCase (path.get (0))) {
				this.path = path.subList (1, path.size ());
				this.details = true;
			} else if ("?".equalsIgnoreCase (path.get (0))) {
				this.path = path.subList (1, path.size ());
				this.details = true;
			} else if ("timing".equalsIgnoreCase (path.get (0))) {
				this.path = path.subList (1, path.size ());
				this.timing = true;
			}
		}
	}
	public IShellRequestSingle (JSONItem json) throws JSONValidationException {
		fromJSON (json);
	}

	public List<String> getPath () {
		return path;
	}

	public ObjectMap getParams () {
		return params;
	}

	public boolean isDetails () {
		return details;
	}

	public boolean isTiming () {
		return timing;
	}

	public String toURL () {
		StringBuilder sb = new StringBuilder ();
		for (String p: path) {
			if (sb.length () > 0) {
				sb.append ("/");
			}
			sb.append (p);
		}
		boolean first = true;
		for (Map.Entry<?, ?> entry: params.entrySet ()) {
			sb.append (first ? "?" : "&");
			first = false;
			sb.append (entry.getKey () + "=" + entry.getValue ());
		}
		return sb.toString ();
	}

	public String getPathString () {
		return parsed.getPathString ();
	}

	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
		super.fromJSON (json);
		
		path = new LinkedList<String> ();
		JSONItem pathj = json.getJSON ("path");
		for (int i=0; i<pathj.length (); ++i) {
			path.add (pathj.getString (i));
		}
		params = new ObjectMap ();
		params.fromJSON (json.getJSON ("params"));
		
		parsed = new ParseRequestResults (json.getString ("pathstring"));
		parsed.setFormat (json.getString ("format"));
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = super.toJSON ();
		json.put ("path", path);
		json.put ("params", params);
		json.put ("format", parsed.getOutputFormat ("default"));
		json.put ("pathstring", parsed.getPathString ());
		return json;
	}
}

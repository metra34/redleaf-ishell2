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

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadWritable;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellRequest implements JSONReadWritable {
	protected boolean usage;
	protected boolean timing;
	protected String remote;

	public boolean isDetails () {
		return usage;
	}

	public boolean isTiming () {
		return timing;
	}

	public void setRemote (String remoteAddr) {
		this.remote = remoteAddr;
	}
	public String getRemote () {
		if (this.remote == null) {
			return "localhost";
		}
		return remote;
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		json.put ("timing", timing);
		json.put ("usage", usage);
		if (remote != null)
			json.put ("remote", remote);
		return json;
	}

	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
		try {
			timing = json.getBoolean ("timing");
		} catch (JSONValidationException e) {
			timing = false;
		}
		
		try {
			usage = json.getBoolean ("usage");
		} catch (JSONValidationException e) {
			usage = false;
		}
		try {
			remote = json.getString ("remote");
		} catch (JSONValidationException e) {
			remote = null;
		}
	}
}

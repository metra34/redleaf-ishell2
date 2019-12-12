/*
 * iShell 2.0
 *
 * Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
 *
 * This library is proprietary software; you can not redistribute
 * without an explicit consent from Redleaf Solutions Ltd.
 * The consent will detail the distribution and sale rights.
 */

package ca.redleafsolutions.ishell2.interfaces;

import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.IShellResponse;
import ca.redleafsolutions.ishell2.ParseRequestResults;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class ConsoleInterface extends IShellInterfaceImpl {
	private JSONItem execlist;

	public ConsoleInterface (iShell main, JSONItem json) throws JSONValidationException {
		super (main, json);
		try {
			this.execlist = json.getJSON ("execute");
		} catch (JSONValidationException e) {
			// Nothing to execute
		}

		if (execlist != null) {
			for (int i = 0; i < execlist.length (); ++i) {
				ParseRequestResults parsed;
				try {
					parsed = new ParseRequestResults (execlist.getString (i));

					IShellRequestSingle request = new IShellRequestSingle (parsed);
					int reqid = iLogger.logIShellRequest (request);

					IShellResponse response = executeAndRespond (request, parsed);
					iLogger.logIShellResponse (reqid, response.toString ());
				} catch (JSONValidationException e) {
					iLogger.severe ("Initial execution prsing failed");
				} catch (Throwable e) {
					iLogger.severe (e);
				}
			}
		}
		
		new ConsoleWorker(this);
	}

	@Override
	public String info () {
		return "Console";
	}

	@Override
	protected String getNativeFormat () {
		return "TEXT";
	}

	@Override
	public boolean isBrowserInterface () {
		return false;
	}
}

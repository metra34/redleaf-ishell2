/*
 * iShell 2.0
 *
 * Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
 *
 * This library is proprietary software; you can not redistribute
 * without an explicit consent from Releaf Solutions Ltd.
 * The consent will detail the distribution and sale rights.
 */

package ca.redleafsolutions.ishell2.utest;

import java.util.ArrayList;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONUtils;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

@SuppressWarnings ("serial")
public class UTestResults extends ArrayList<UTestResult> implements JSONWritable {
	private String name;
	private long tic;
	private long duration;

	public UTestResults (String name) {
		this.name = name;
	}

	@Override
	public String toString () {
		String s = "[" + (isPass () ? "PASS" : "FAIL") + "]" + ((name != null) ? " " + name : "") + " ("
				+ (duration / 1000000.) + "/" + (getDuration () / 1000000.) + " ms)";
		for (UTestResult testresult: this) {
			s += "\n" + testresult;
		}
		return s;
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		json.put ("pass", isPass ());
		if (name != null) {
			json.put ("name", name);
		}
		JSONItem jsonarr = JSONItem.newArray ();
		json.put ("tests", jsonarr);
		for (Object item: this) {
			jsonarr.put (JSONUtils.toJSON (item));
		}
		json.put ("duration", duration / 1000000.);
		json.put ("duration-net", getDuration () / 1000000.);
		return json;
	}

	private boolean isPass () {
		boolean pass = true;
		for (UTestResult item: this) {
			pass &= item.isPass ();
		}
		return pass;
	}

	private long getDuration () {
		long duration = 0;
		for (UTestResult item: this) {
			duration += item.getDuration ();
		}
		return duration;
	}

	public void tic () {
		tic = System.nanoTime ();
	}

	public void toc () {
		duration = System.nanoTime () - tic;
	}
}

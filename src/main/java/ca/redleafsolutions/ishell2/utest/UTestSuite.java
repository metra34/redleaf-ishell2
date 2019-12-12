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

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.IShellInputStream;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadWritable;
import ca.redleafsolutions.json.JSONValidationException;

public class UTestSuite implements JSONReadWritable {
	private ObjectMap map;
	private String name;
	private List<UTest> tests;

	public UTestSuite (JSONItem json) throws JSONValidationException {
		fromJSON (json);
		map = new ObjectMap ();
	}

	public String name () {
		return name;
	}
	
	public List<UTest> tests () {
		return tests;
	}
	
	public void setTrace (boolean trace) {
		for (UTest test: tests) {
			test.setTrace (trace);
		}
	}

	public UTestResults run () throws FileNotFoundException, JSONValidationException {
		UTestResults results = new UTestResults (name);
		results.tic ();
		for (UTest test: tests) {
			if (!test.skip()) {
				test.run (map);
				results.add (test.getResult ());
			}
		}
		results.toc ();
		return results;
	}

	public IShellInputStream runThread () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
		try {
			this.name = json.getString ("name");
		} catch (JSONValidationException e) {
			name = null;
		}
		
		ObjectMap map = new ObjectMap ();
		try {
			map.fromJSON (json.getJSON ("data"));
		} catch (Exception e) {
		}

		tests = new LinkedList<UTest> ();
		JSONItem tests = json.getJSON ("tests");
		for (int i = 0; i < tests.length (); ++i) {
			JSONItem testj = tests.getJSON (i);
			this.tests.add (new UTest (testj, map));
		}

		try {
			if (json.getBoolean ("trace")) {
				setTrace (true);
			}
		} catch (JSONValidationException e) {
		}
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		json.put ("name", name);
		JSONItem jsontests = JSONItem.newArray ();
		json.put ("tests", jsontests);
		for (UTest test: tests) {
			jsontests.put (test.toJSON ());
		}
		json.put ("trace", false);
		return json;
	}
}

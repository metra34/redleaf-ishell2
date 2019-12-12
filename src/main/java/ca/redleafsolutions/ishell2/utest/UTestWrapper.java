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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.HTMLWritable;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class UTestWrapper {
	@SuppressWarnings ("serial")
	public class Collection extends LinkedList<String> implements HTMLWritable {
		private UTestWrapper wrapper;

		public Collection (UTestWrapper wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		public String toHTML () {
			String path = this.getClass ().getPackage ().getName ().replace ('.', '/') + "/utestlist.html";
			InputStream is = this.getClass ().getClassLoader ().getResourceAsStream (path);
			if (is == null)
				return toString ();
			ObjectMap map = new ObjectMap ();
			map.put ("tests", wrapper.list ());
			map.put ("utest", wrapper);
			return TemplateUtils.evaluate (is, map);
		}
	}

	static private final String EXT = ".test";
	private File root;
	private boolean trace;

	public UTestWrapper (JSONItem json) throws JSONValidationException {
		this.root = new File (json.getString ("root"));
	}

	public UTestWrapper.Collection list () {
		UTestWrapper.Collection tests = new UTestWrapper.Collection (this);
		File[] files = root.listFiles (new FileFilter () {
			@Override
			public boolean accept (File file) {
				String fname = file.getName ();
				return fname.toLowerCase ().lastIndexOf (EXT) == fname.length () - EXT.length ();
			}
		});
		if (files != null) {
			for (File file: files) {
				String fname = file.getName ();
				tests.add (fname.substring (0, fname.length () - EXT.length ()));
			}
		}
		return tests;
	}

	public UTestSuite get (String name) throws IOException, FileNotFoundException, JSONValidationException {
		File file = new File (root, name + EXT);
		if (!file.exists ())
			throw new FileNotFoundException (file.toString ());
		return new UTestSuite (JSONItem.fromFile (file));
	}

	public void trace (String state) throws IllegalArgumentException {
		if ("on".equals (state) || "true".equals (state)) {
			this.trace = true;
		} else if ("off".equals (state) || "false".equals (state)) {
			this.trace = false;
		} else {
			throw new IllegalArgumentException ("state must be on/off/true/false");
		}
	}

	@MethodDescription ("Run a UTest suite by name of JSON data")
	@ParameterNames ("nameorjson")
	@ParameterDescriptions ("Name of stored UTest or JSON data of the UTest to run")
	public UTestResults run (String nameorjson) throws JSONValidationException, IOException {
		UTestSuite utest;
		try {
			utest = get (nameorjson);
		} catch (FileNotFoundException e) {
			utest = new UTestSuite (JSONItem.parse (nameorjson));
		}

		if (trace)
			utest.setTrace (true);

		return utest.run ();
	}
}

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

import java.util.LinkedList;
import java.util.List;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONUtils;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class UTestResult implements JSONWritable {
	private String name;
	private String command;
	private List<UTestExpectedResult> expected;
	private boolean pass;
	private long duration;
	private Object result;
	private boolean trace;

	public UTestResult (String name, String command, Object expecto, ObjectMap map, boolean trace) throws JSONValidationException {
		this.name = name;
		this.command = command;
		this.expected = new LinkedList<UTestExpectedResult> ();

		if (expecto instanceof String) {
			String str = TemplateUtils.evaluate ((String)expecto, map);
			expected.add (new UTestExpectedValue (str));
		} else if (expecto instanceof JSONItem) {
			JSONItem json = (JSONItem)expecto;
			String str = TemplateUtils.evaluate (json.toString (), map);
			json = JSONItem.parse (str);
			json = JSONItem.forceJSONArray (json);

			for (int i = 0; i < json.length (); ++i) {
				try {
					expected.add (new UTestExpectJSON (json.getJSON (i), map));
				} catch (JSONValidationException e) {
					expected.add (new UTestExpectedValue (json.getString (i)));
				}
			}
		}
		setTrace (trace);
	}

	public UTestResult (String name, String command, Object expecto, ObjectMap map,
			IShellObject executed, long duration, boolean trace) throws JSONValidationException {
		this (name, command, expecto, map, trace);
		//
		//		if (expecto != null) {
		//			if (expecto instanceof JSONArray) {
		//				expecto = JSONUtils.json2item ((JSONArray)expecto);
		//			} else if (expecto instanceof JSONObject) {
		//				expecto = JSONUtils.json2item ((JSONObject)expecto);
		//			}
		//			
		//			if (expecto instanceof JSONItem) {
		//				JSONItem jarr = JSONUtils.forceJSONArray ((JSONItem)expecto);
		//				for (int i = 0; i < jarr.length (); ++i) {
		//					try {
		//						expected.add (new UTestExpectJSON (jarr.getJSON (i), map));
		//					} catch (Exception e) {
		//						expected.add (new UTestExpectedValue (jarr.getString (i)));
		//					}
		//				}
		//			} else {
		//				expected.add (new UTestExpectedValue ((String)expecto));
		//			}
		//		} else {
		//		}

		result (executed, duration);
	}

	public boolean isPass () {
		return pass;
	}

	public void setTrace (boolean trace) {
		this.trace = trace;
	}

	public void result (IShellObject o, long duration) {
		this.duration = duration;

		if (o instanceof IShellObject.ExecutedObject) {
			result = ((IShellObject.ExecutedObject)o).getObject ();
			pass = true;
			if (expected.size () > 0) {
				for (UTestExpectedResult exp:expected) {
					pass &= exp.asExpected (result);
				}
			}
		} else if (o instanceof IShellObject.RawObject) {
			throw new RuntimeException ("NIY");
		} else if (o instanceof IShellObject.ExceptionObject) {
			result = ((IShellObject.ExceptionObject)o).getObject ();
			pass = expected.size () <= 0? true: false;
		}
	}

	public void result (IShellException e, ObjectMap map, long duration) {
		this.duration = duration;

		if (e instanceof IShellException.InvocationTargetException) {
			result = ((IShellException.InvocationTargetException)e).getException ();
		} else {
			result = e;
		}
		String clsname = result.getClass ().getName ();
		for (UTestExpectedResult exp:expected) {
			pass |= exp.asExpected (clsname);
		}
	}

	@Override
	public String toString () {
		String s = "";
		if (trace) {
			s += "<- " + command + "\n";
			if (result != null) {
				if (result.getClass ().isPrimitive () || (result instanceof String)) {
					s += "-> " + result + "\n";
				} else {
					try {
						s += "-> " + JSONUtils.toJSON (result) + "\n";
					} catch (JSONValidationException e) {
						s += "-> '" + result + "' caused " + e + "\n";
					}
				}
			}
		}
		s += "- [" + (pass? ((expected.size () <= 0)? "DONE": "PASS"): "FAIL") + "] ";
		if (name != null) {
			s += name;
		} else {
			s += command;
		}
		s += " (" + ((duration / 1000) / 1000.) + " ms)";
		return s;
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();

		if (name != null) {
			json.put ("name", name);
		}
		json.put ("cmd", command);
		if ((expected != null) && (expected.size () > 0)) {
			JSONItem jexp = JSONItem.newArray ();
			json.put ("expect", jexp);
			for (UTestExpectedResult exp:expected) {
				jexp.put (exp.toString ());
			}
		}
		if (this.trace) {
			json.put ("result", this.result);
		}

		json.put ("duration", duration / 1000000.);
		json.put ("pass", pass);
		if (result == null) {
		} else if (result instanceof Throwable) {
			Throwable e = (Throwable)result;
			if (e instanceof IShellException.InvocationTargetException) {
				e = ((IShellException.InvocationTargetException)e).getException ();
			}
			JSONItem ejson = JSONItem.newObject ();
			ejson.put ("type", e.getClass ().getName ());
			ejson.put ("message", e.getMessage ());
			ejson.put ("stacktrace", e.getStackTrace ());
			json.put ("exception", ejson);
		} else {
			if (!pass)
				json.put ("result", result.toString ());
		}
		return json;
	}

	public String getCommand () {
		return command;
	}

	public long getDuration () {
		return duration;
	}

	public void setCommand (String command) {
		this.command = command;
	}
}

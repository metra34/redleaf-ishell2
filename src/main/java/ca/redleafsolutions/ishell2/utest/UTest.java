package ca.redleafsolutions.ishell2.utest;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.ParseRequestResults;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadWritable;
import ca.redleafsolutions.json.JSONValidationException;

public class UTest implements JSONReadWritable {
	private String name;
	private String command;
	private String resvar;
	private UTestResult utestresult;
	private boolean trace;
	private int wait;
	private IShellObject executed;
	private Object expectedjson;
	private boolean _skip;
	private ObjectMap data;

	public UTest (JSONItem json, ObjectMap map) throws JSONValidationException {
		this.data = map;
		fromJSON (json);
	}

	public void run (ObjectMap map) {
		map.putAll (data);
		String command = TemplateUtils.evaluate (this.command, map);

		IShellRequestSingle request = new IShellRequestSingle (new ParseRequestResults (command));

		long tic = System.nanoTime ();
		if (wait > 0) {
			try {
				Thread.sleep (wait);
			} catch (InterruptedException e) {
				try {
					utestresult = new UTestResult (name, command, (String)null, map, true);
					utestresult.result (new IShellObject.ExceptionObject (e, request), System.nanoTime () - tic);
					iLogger.info ("UTest wait exception -> " + e);
				} catch (JSONValidationException e1) {
					iLogger.info ("UTest wait exception -> " + e);
				}
			}
		}

		tic = System.nanoTime ();
		try {
			try {
				iLogger.info ("UTest request <- " + command);
				executed = iShell.getInstance ().execute (request);

				if (resvar != null) {
					map.put (resvar, executed.getObject ());
				}

				utestresult = new UTestResult (name, command, expectedjson, map, executed, System.nanoTime () - tic, trace);
				if (utestresult.isPass ()) {
					iLogger.info ("UTest [PASS] response -> " + executed.getObject ());
				} else {
					iLogger.info ("UTest [FAIL] response -> " + executed.getObject ());
				}
			} catch (IShellException e) {
				utestresult = new UTestResult (name, command, expectedjson, map, trace);
				utestresult.result (e, map, System.nanoTime () - tic);
			}
		} catch (JSONValidationException e) {
			try {
				utestresult = new UTestResult (name, command, expectedjson, map, trace);
				utestresult.result (new IShellObject.ExceptionObject (e, request), System.nanoTime () - tic);
				iLogger.severe ("UTest exception while building response -> " + e);
			} catch (JSONValidationException e1) {
				iLogger.severe ("UTest exception while building response -> " + e);
			}
		}
	}

	public UTestResult getResult () /* throws TestNotRunYet */{
		// if (executed == null)
		// throw new UTestException.TestNotRunYet (this);
		return utestresult;
	}

	public String getName () {
		return name;
	}

	public String getCommand () {
		return command;
	}

	public void setTrace (boolean trace) {
		this.trace = trace;
	}

	public boolean skip () {
		return _skip;
	}

	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
		this.command = json.getString ("cmd");

		try {
			this.name = json.getString ("name");
		} catch (JSONValidationException e) {
			this.name = command;
		}

		try {
			this._skip = json.getBoolean ("skip");
		} catch (JSONValidationException e) {
			this._skip = false;
		}

		try {
			this.resvar = json.getString ("return");
		} catch (JSONValidationException e) {
			try {
				this.resvar = json.getString ("resvar");
			} catch (JSONValidationException e1) {
				this.resvar = null;
			}
		}

		try {
			this.wait = json.getInt ("wait");
		} catch (JSONValidationException e) {
			this.wait = 0;
		}

		try {
			expectedjson = json.get ("expect");
		} catch (JSONValidationException e) {
			expectedjson = null;
		}
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();

		json.put ("cmd", this.command);
		if (!name.equals (command)) {
			json.put ("name", this.name);
		}

		if (_skip)
			json.put ("skip", this._skip);
		json.put ("return", this.resvar);

		if (wait > 0) {
			json.put ("wait", this.wait);
		}

		if (expectedjson != null) {
			json.put ("expect", expectedjson);
		}
		return json;
	}
}

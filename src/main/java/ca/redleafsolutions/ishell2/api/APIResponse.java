package ca.redleafsolutions.ishell2.api;

import ca.redleafsolutions.ObjectList;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class APIResponse<T extends JSONWritable> implements JSONWritable {
	protected T o;
	protected Exception e;
	private HTTP_STATUS_CODES httpcode = HTTP_STATUS_CODES.OK;
	protected APIInterface parent;

	protected APIResponse (APIInterface parent) {
		this.parent = parent;
	}

	public APIResponse (APIInterface parent, T o) {
		this (parent);
		this.o = o;
		this.e = null;
	}

	public APIResponse (Exception e) {
		withFailure (e, HTTP_STATUS_CODES.INTERNAL_SERVER_ERROR);
	}

	public APIResponse (Exception e, HTTP_STATUS_CODES code) {
		withFailure (e, code);
	}

	@IShellInvisible
	public T getObject () {
		return o;
	}

	@IShellInvisible
	public HTTP_STATUS_CODES getHTTPCode () {
		return httpcode;
	}

	public APIResponse<T> withHTTPStatusCode (HTTP_STATUS_CODES code) {
		httpcode = code;
		return this;
	}

	public APIResponse<T> withFailure (Exception e) {
		return withFailure (e, HTTP_STATUS_CODES.INTERNAL_SERVER_ERROR);
	}

	public APIResponse<T> withFailure (Exception e, HTTP_STATUS_CODES code) {
		this.e = e;
		this.o = null;
		this.httpcode = code;
		iLogger.severe (e);
		return this;
	}

	@Override
	@IShellInvisible
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		if (o != null) {
			json.put ("success", true);
			if (!(o instanceof VOIDAPIResponse))
				json.put ("result", o.toJSON ());
		} else if (e != null) {
			json.put ("success", false);
			json.put ("exception", (e instanceof JSONWritable)? ((JSONWritable)e).toJSON (): e.getClass ().getSimpleName ());
			json.put ("message", e.getMessage ());
			json.put ("response-code", httpcode.getCode ());

			if (APIResponseSettings.debug) {
				ObjectList stacktrace = new ObjectList ();
				json.put ("stack", stacktrace);
				for (StackTraceElement ste:e.getStackTrace ()) {
					ObjectMap stemap = new ObjectMap ();
					stacktrace.push (stemap);
					stemap.put ("class", ste.getClassName ());
					stemap.put ("file", ste.getFileName ());
					stemap.put ("line", ste.getLineNumber ());
					stemap.put ("method", ste.getMethodName ());
				}
			}
		} else {
			json.put ("success", false);
			json.put ("exception", "unknown");
		}
		return json;
	}
	
	@Override
	@IShellInvisible
	public String toString () {
		if (o != null) {
			return o.toString ();
		} else {
			return httpcode.getCode () + ": " + e.toString ();
		}
	}
}

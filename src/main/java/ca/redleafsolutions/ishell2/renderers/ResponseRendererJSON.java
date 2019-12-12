/*
 * iShell 2.0
 *
 * Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
 *
 * This library is proprietary software; you can not redistribute
 * without an explicit consent from Releaf Solutions Ltd.
 * The consent will detail the distribution and sale rights.
 */

package ca.redleafsolutions.ishell2.renderers;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellMethod;
import ca.redleafsolutions.ishell2.IShellObject.ExecutedObject;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONUtils;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class ResponseRendererJSON extends ResponseRendererBase {
	public ResponseRendererJSON (double timing) {
		super (timing);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public String toString (Object o, IShellRequest request) {
		try {
			JSONItem json;

			if (o instanceof Throwable) {
				json = toJSONErrorString ((Throwable)o);
			} else {
				if (request.isDetails ()) {
					json = _toDetails (o, request);
				} else {
					if (o instanceof ExecutedObject) {
						ExecutedObject exobj = (ExecutedObject)o;
						o = exobj.getObject ();
					}

					if (o instanceof JSONItem) {
						json = (JSONItem)o;
						return json.toString (3);
					} else if (o instanceof JSONWritable) {
						return ((JSONWritable)o).toJSON ().toString (3);
					} else {
						json = JSONItem.newObject ();
						if (o == null) {
							json.put ("result", "");
						} else if (o instanceof String) {
							json.put ("result", o.toString ());
						} else if (o instanceof Character) {
							json.put ("result", o.toString ());
						} else if (o instanceof Number) {
							int i = ((Number)o).intValue ();
							double d = ((Number)o).doubleValue ();
							if (i == d) {
								json.put ("result", i);
							} else {
								json.put ("result", d);
							}
						} else if (o instanceof Boolean) {
							json.put ("result", ((Boolean)o).booleanValue ());
						} else if (o instanceof JSONWritable) {
							json = ((JSONWritable)o).toJSON ();
						} else if (o instanceof Map) {
							for (Entry<? extends Object, ? extends Object> entry:((Map<? extends Object, ? extends Object>)o)
									.entrySet ()) {
								Object key = entry.getKey ();
								if (key == null)
									key = "null";
								Object value = entry.getValue ();
								if (value == null)
									value = "null";
								json.put (key.toString (), JSONUtils.toJSON (value));
							}
						} else if (o instanceof Iterable) {
							JSONItem jsonarr = JSONItem.newArray ();
							for (Object item:(Iterable<? extends Object>)o) {
								jsonarr.put (JSONUtils.toJSON (item));
							}
							return jsonarr.toString (3);
						} else if (o.getClass ().isArray ()) {
							JSONItem arr = JSONItem.newArray ();
							for (Object item:(Object[])o) {
								arr.put (item);
							}
							return arr.toString (3);
						} else {
							json = _toDetails (o, request);
						}
					}
				}
			}
			if ((timing >= 0) && json.isObject ()) {
				JSONItem timingjson = JSONItem.newObject ();
				json.put ("timing", timingjson);
				timingjson.put ("duration", timing);
				timingjson.put ("units", "ms");
				try {
					Runtime runtime = Runtime.getRuntime ();
					timingjson.put ("freemem", runtime.freeMemory ());
				} catch (Throwable e) {
				}
			}
			return json.toString (3);
		} catch (JSONValidationException e) {
			return "{ status:\"error\", type:\"" + e.getClass ().getSimpleName () + ", message:\"" + e.getMessage ()
					+ "\" }";
		}
	}

	//
	// private Object toJSONValue (Object o) throws JSONException {
	// return toJSONValue (o, 0);
	// }
	//
	// @SuppressWarnings ("unchecked")
	// private Object toJSONValue (Object o, int depth) throws JSONException {
	// if (depth > 2)
	// return o;
	//
	// if (o instanceof JSONWritable) {
	// return ((JSONWritable)o).toJSON ();
	// }
	//
	// if (o instanceof Map) {
	// JSONObject json = new JSONObject ();
	// for (Entry<? extends Object, ? extends Object> entry:((Map<? extends
	// Object, ? extends Object>)o).entrySet ()) {
	// json.put (entry.getKey ().toString (), toJSONValue (entry.getValue (),
	// depth+1));
	// }
	// return json;
	// } else if (o instanceof Iterable) {
	// JSONArray jsonarr = new JSONArray ();
	// for (Object item:(Iterable<? extends Object>)o) {
	// jsonarr.put (toJSONValue (item, depth+1));
	// }
	// return jsonarr.toString (3);
	// }
	// return o;
	// }
	//
	//
	// private JSONObject toJSONObject (Object o) throws JSONValidationException
	// {
	// if (o instanceof JSONObject) {
	// return (JSONObject)o;
	// }
	// if (o instanceof JSONWritable) {
	// return ((JSONWritable)o).toJSON ();
	// }
	//
	// if (o == null) {
	// o = new NullResponse ();
	// }
	// JSONObject json = new JSONObject ();
	// JSONObject type = new JSONObject ();
	// try {
	// json.put ("class", type);
	// type.put ("class", o.getClass ().getSimpleName ());
	// Class<?> enclosing = o.getClass ().getEnclosingClass ();
	// if (enclosing != null) {
	// type.put ("enclosing", enclosing.getSimpleName ());
	// }
	// Package pkg = o.getClass ().getPackage ();
	// type.put ("package", pkg != null ? pkg.getName () : "default");
	//
	// if (o.getClass ().isArray ())
	// json.put ("array", true);
	// JSONObject fields = new JSONObject ();
	// json.put ("fields", fields);
	// for (Entry<String, Object> entry: getFields (o).entrySet ()) {
	// JSONObject fieldObj = new JSONObject ();
	// Object value = entry.getValue ();
	// if (value != null) {
	// fieldObj.put ("type", value.getClass ().getSimpleName ());
	// }
	// fieldObj.put ("value", value);
	// fields.put (entry.getKey (), fieldObj);
	// }
	//
	// JSONArray functions = new JSONArray ();
	// json.put ("functions", functions);
	// for (IShellMethod method: getMethods (o)) {
	// JSONObject funcObj = new JSONObject ();
	// funcObj.put ("name", Object.class.getName ());
	// functions.put (method.toJSON ());
	// }
	// } catch (JSONException e) {
	// throw new JSONValidationException (e);
	// }
	// return json;
	// }

	private JSONItem toJSONErrorString (Throwable o) throws JSONValidationException {
		if (o instanceof IShellException.InvocationTargetException) {
			return toJSONErrorString (((IShellException.InvocationTargetException)o).getException ());
		}
		JSONItem json = JSONItem.newObject ();
		json.put ("status", "error");
		json.put ("class", o.getClass ().getSimpleName ());
		if (o.getMessage () != null) {
			json.put ("message", o.getMessage ());
		}
		if (o.getCause () != null) {
			Throwable cause = o.getCause ();
			JSONItem causej = JSONItem.newObject ();
			json.put ("cause", causej);
			causej.put ("class", cause.getClass ().getSimpleName ());
			if (cause.getMessage () != null) {
				causej.put ("message", cause.getMessage ());
			}
		}
		//		StackTraceElement[] trace = o.getStackTrace ();
		//		JSONItem tracearray = JSONItem.newArray ();
		//		json.put ("trace", tracearray);
		//		for (StackTraceElement tr: trace) {
		//			JSONItem jsontr = JSONItem.newObject ();
		//			if (!tr.isNativeMethod ()) {
		//				jsontr.put ("file", tr.getFileName ());
		//				jsontr.put ("line", tr.getLineNumber ());
		//				jsontr.put ("class", tr.getClassName ());
		//				jsontr.put ("method", tr.getMethodName ());
		//				tracearray.put (jsontr);
		//			}
		//		}

		return json;
	}

	@Override
	public String toExceptionString (Throwable o, IShellRequest request) {
		try {
			JSONItem json = toJSONErrorString (o);
			return json.toString (3);
		} catch (JSONValidationException e) {
			return "{ status:\"error\", type:\"" + e.getClass ().getSimpleName () + ", message:\"" + e.getMessage ()
					+ "\" }";
		}
	}

	@Override
	public String toDetails (Object o, IShellRequest request) {
		try {
			return _toDetails (o, request).toString (3);
		} catch (JSONValidationException e) {
			return "{ status:\"error\", type:\"" + e.getClass ().getSimpleName () + ", message:\"" + e.getMessage ()
					+ "\" }";
		}
	}

	private JSONItem _toDetails (Object o, IShellRequest request) throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		json.put ("array", o.getClass ().isArray ());
		Class<?> enclosing = o.getClass ().getEnclosingClass ();
		if (enclosing != null) {
			json.put ("enclosing", enclosing.getSimpleName ());
		}
		json.put ("class", o.getClass ().getSimpleName ());
		Package pkg = o.getClass ().getPackage ();
		json.put ("package", (pkg == null)? "default": pkg.getName ());

		Map<String, Object> fields = getFields (o);
		if (fields.size () > 0) {
			JSONItem jsonfields = JSONItem.newObject ();
			json.put ("fields", jsonfields);
			for (Entry<String, Object> entry:fields.entrySet ()) {
				String key = entry.getKey ();
				Object value = entry.getValue ();
				JSONItem jsonfield = JSONItem.newObject ();

				jsonfields.put (key, jsonfield);
				jsonfield.put ("class", value.getClass ().getSimpleName ());
				if (!value.getClass ().isPrimitive ())
					jsonfield.put ("package", value.getClass ().getPackage ().getName ());
				jsonfield.put ("value", value);
			}
		}

		Collection<IShellMethod> methods = getMethods (o);
		if (methods.size () > 0) {
			JSONItem jsonmethods = JSONItem.newObject ();
			json.put ("methods", jsonmethods);
			for (IShellMethod method:methods) {
				jsonmethods.put (method.getName (), method.toJSON ());
			}
		}
		return json;
	}
}

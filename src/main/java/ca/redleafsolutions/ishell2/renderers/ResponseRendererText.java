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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.redleafsolutions.ishell2.IShellInputStream;
import ca.redleafsolutions.ishell2.IShellMethod;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.logs.iLogger;

public class ResponseRendererText extends ResponseRendererBase {
	public ResponseRendererText (double timing) {
		super (timing);
	}

	@Override
	public String toString (Object o, IShellRequest request) {
		String s = "";
		if (o instanceof IShellObject) {
			IShellObject ishello = (IShellObject)o;
			if (ishello.isExecuted ()) {
				s = _toString (ishello.getObject (), request);
			} else if (ishello.isRaw ()) {
				s = toDetails (ishello, request);
			} else if (ishello.isException ()) {
				s = toExceptionString ((Throwable)ishello.getObject (), request);
			}
		} else {
//			s = toDetails (o, request);
			s = _toString (o, request);
		}
		if (o instanceof IShellInputStream) {
			try {
				((IShellInputStream)o).close ();
			} catch (IOException e) {
				iLogger.severe ("Failed to close input stream in iShell text renderer");
			}
		}
		return s;
	}

	@SuppressWarnings ("unchecked")
	private String _toString (Object o, IShellRequest request) {
		if (o == null)
			return "";

		Method toStringMethod;
		try {
			toStringMethod = o.getClass ().getDeclaredMethod ("toString", new Class<?>[] {});
		} catch (NoSuchMethodException | SecurityException e1) {
			toStringMethod = null;
		}

		String s = "";
		if (toStringMethod != null) {
			s += o.toString ();
		} else if (o instanceof Number) {
			s += o;
		} else if (o instanceof String) {
			s += o;
		} else if (o instanceof Boolean) {
			s += o;
		} else if (o instanceof Map) {
			for (Entry<? extends Object, ? extends Object> entry: ((Map<? extends Object, ? extends Object>)o)
					.entrySet ()) {
				s += iShell.lineSeparater + entry.getKey () + ": " + entry.getValue ();
			}
		} else if (o instanceof Iterable) {
			for (Object item: (Iterable<? extends Object>)o) {
				s += iShell.lineSeparater + item;
			}
		} else if (o.getClass ().isArray ()) {
			List<Object> list = Arrays.asList ((Object[])o);
			for (Object item: list) {
				s += iShell.lineSeparater + item;
			}
		} else {
//			try {
//				if (o instanceof JSONObject) {
//					s += ((JSONObject)o).toString (3);
//					return s;
//				} else if (o instanceof JSONWritable) {
//					s += ((JSONWritable)o).toJSON ().toString (3);
//					return s;
//				}
//			} catch (JSONException | JSONValidationException e) {
//			}
//
			Method tostring;
			try {
				tostring = o.getClass ().getDeclaredMethod ("toString");
				if (tostring != null) {
					s = o.toString ();
				} else {
					return toDetails (o, request);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				return toDetails (o, request);
			}
		}

		if (timing >= 0) {
			s += iShell.lineSeparater + "* Duration: " + timing + " milliseconds";
			s += ", Memory: " + (((int)(Runtime.getRuntime ().freeMemory () / 102.4)) / 10.) + "K";
		}
		return s;
	}

	@Override
	public String toExceptionString (Throwable e, IShellRequest request) {
		return "EXECUTION ERROR: " + e;
	}

	@Override
	public String toDetails (Object o, IShellRequest request) {
		String s = "";
		if (o != null) {
			if (o.getClass ().isArray ())
				s += "Array of ";
	
			Class<?> enclosing = o.getClass ().getEnclosingClass ();
			if (enclosing != null) {
				s += enclosing.getSimpleName () + ".";
			}
			s += o.getClass ().getSimpleName ();
			Package pkg = o.getClass ().getPackage ();
			s += " (" + ((pkg == null) ? "default package" : pkg.getName ()) + ")";
	
			Map<String, Object> fields = getFields (o);
			if (fields.size () > 0) {
				s += iShell.lineSeparater + "Fields:";
				for (Entry<String, Object> entry: fields.entrySet ()) {
					String key = entry.getKey ();
					Object value = entry.getValue ();
					s += iShell.lineSeparater + "\t" + key + " (" + ((value != null) ? value.getClass ().getSimpleName () : "null")
							+ ") - " + value;
				}
			}
	
			Collection<IShellMethod> methods = getMethods (o);
			if (methods.size () > 0) {
				s += iShell.lineSeparater + "Functions:";
				for (IShellMethod method: methods) {
					s += iShell.lineSeparater + "\t" + method.toString ();
				}
			}
		}
		return s;
	}
}

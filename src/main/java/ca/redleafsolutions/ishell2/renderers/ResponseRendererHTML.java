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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.redleafsolutions.ishell2.HTMLWritable;
import ca.redleafsolutions.ishell2.IShellMethod;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.api.APIResponse;

public class ResponseRendererHTML extends ResponseRendererBase {
	public ResponseRendererHTML (double timing) {
		super (timing);
	}

	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public String toString (Object o, IShellRequest request) {
		String s = "<html><body>";

		if (o instanceof APIResponse) {
			o = ((APIResponse)o).getObject ();
		}

		if (o == null) {
			s += "";
		} else if (o instanceof HTMLWritable) {
			s += ((HTMLWritable)o).toHTML ();
		} else if (o instanceof String) {
			s += o.toString ();
		} else if (o instanceof Character) {
			s += o.toString ();
		} else if (o instanceof Number) {
			s += o.toString ();
		} else if (o instanceof Boolean) {
			s += o.toString ();
		} else if (o instanceof Map) {
			s += "<table>";
			for (Entry<? extends Object, ? extends Object> entry:((Map<? extends Object, ? extends Object>)o)
					.entrySet ()) {
				s += "<tr><td>" + entry.getKey () + "<td>" + entry.getValue ();
			}
			s += "</table>";
		} else if (o instanceof Iterable) {
			for (Object item:(Iterable<? extends Object>)o) {
				s += "<li>" + item;
			}
		} else if (o.getClass ().isArray ()) {
			List<Object> list = Arrays.asList ((Object[])o);
			for (Object item:list) {
				s += "<li>" + item;
			}
		} else {
			s += toDetails (o, request);
		}

		if (this.timing >= 0) {
			s += "<br><i>Execution time: " + timing + " milliseconds";
			s += ", Memory: " + (((int)(Runtime.getRuntime ().freeMemory () / 102.4)) / 10.) + "K</i>";
		}
		s += "</body></html>";
		return s;
	}

	@Override
	public String toExceptionString (Throwable o, IShellRequest request) {
		String s = "<html><body>";
		s += "<h1>";
		s += o.getClass ().getSimpleName ();
		s += "</h1>";
		s += "<h2>";
		String msg = o.getMessage ();
		Throwable cause = o.getCause ();
		s += (msg != null)? o.getMessage (): (cause != null)? cause.toString (): o.toString ();
		s += "</h2>";
		s += "<a href='#' onclick='history.back(); return false;'>Back</a>";
		s += "</body></html>";
		return s;
	}

	@Override
	public String toDetails (Object o, IShellRequest request) {
		String s = "<h1>";
		if (o.getClass ().isArray ())
			s += "Array of ";
		Class<?> enclosing = o.getClass ().getEnclosingClass ();
		if (enclosing != null) {
			s += enclosing.getSimpleName () + ".";
		}
		s += o.getClass ().getSimpleName ();
		Package pkg = o.getClass ().getPackage ();
		s += " (" + ((pkg == null)? "default package": pkg.getName ()) + ")";
		s += "</h1>";

		Map<String, Object> fields = getFields (o);
		if (fields.size () > 0) {
			s += "<h2>Fields</h2>";
			s += "<table border=0>";
			for (Entry<String, Object> entry:fields.entrySet ()) {
				String key = entry.getKey ();
				Object value = entry.getValue ();
				s += "<tr valign='top'><td>" + key + "<td>"
						+ ((value != null)? value.getClass ().getSimpleName (): "")
						+ "<td><nobr><form><input type='text' name='" + key + "' value='" + value
						+ "'><input type='submit' value='change'></form></nobr>";
			}
			s += "</table>";
		}

		Collection<IShellMethod> methods = getMethods (o);
		if (methods.size () > 0) {
			s += "<h2>Methods</h2>";
			s += "<table border=0>";
			for (IShellMethod method:methods) {
				s += "<tr valign=middle><td>" + method.toHTML ();
			}
			s += "</table>";
		}
		return s;
	}

}

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
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellMethod;
import ca.redleafsolutions.ishell2.IShellRequest;

public class ResponseRendererHTMLDoc extends ResponseRendererBase {
	public ResponseRendererHTMLDoc (double timing) {
		super (timing);
	}

	@Override
	public String toString (Object o, IShellRequest request) {
		String s = "<html><body>";

		if (o == null) {
			s += "NULL!!!";
		} else {
			ObjectMap map = new ObjectMap ();
			Class<? extends Object> cls = o.getClass ();
			map.put ("class", cls);
			map.put ("fields", cls.getFields ());
			map.put ("methods", getMethods (o));
			map.put ("modifiers", cls.getModifiers ());
			map.put ("anotations", cls.getAnnotations ());

			ResourceLocator locator = new ResourceLocator (this.getClass ());

			InputStream is = locator.getInputStream ("documentator.html");
			if (is != null) {
				try {
					return TemplateUtils.evaluate (is, map);
				} finally {
					try {
						is.close ();
					} catch (IOException e) {
					}
				}
			}

			s += "<h1>";
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
		s += o.getMessage ();
		s += "</h2>";
		s += "<a href='#' onclick='history.back(); return false;'>Back</a>";
		s += "</body></html>";
		return s;
	}

	@Override
	public String toDetails (Object o, IShellRequest request) {
		return toString (o, request);
	}

}

/*
 * iShell 2.0
 *
 * Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
 *
 * This library is proprietary software; you can not redistribute
 * without an explicit consent from Releaf Solutions Ltd.
 * The consent will detail the distribution and sale rights.
 */

package ca.redleafsolutions.ishell2;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class IShellMethod implements Comparable<IShellMethod>, JSONWritable, HTMLWritable {
	private String description;
	private Collection<IShellMethod.Parameter> params = new ArrayList<> ();
	private Collection<Class<?>> excetions = new ArrayList<> ();
	private String name;
	private Method method;
	private ObjectMap rettype;

	public Method getMethod () {
		return method;
	}

	public String getName () {
		return name;
	}

	public String getDescription () {
		return description;
	}

	public Collection<IShellMethod.Parameter> getParameters () {
		return params;
	}

	public Collection<Class<?>> getExceptions () {
		return excetions;
	}

	public ObjectMap getRetType () {
		return rettype;
	}

	public IShellMethod (Method method) throws Exception {
		this.method = method;
		this.name = method.getName ();

		if (method.isAnnotationPresent (IShellInvisible.class)) {
			throw new Exception ("Avoid reflecting this method");
		}

		MethodDescription annmethod = method.getAnnotation (MethodDescription.class);
		if (annmethod != null) {
			description = annmethod.value ();
		}

		Class<?>[] parameters = method.getParameterTypes ();
		for (Class<?> p:parameters) {
			if (!p.isPrimitive () && !p.isAssignableFrom (String.class) && !p.isAssignableFrom (Number.class)
					&& !p.isAssignableFrom (Boolean.class) && !p.isAssignableFrom (Character.class)
					&& !p.isAssignableFrom (CharSequence.class) && !p.isAssignableFrom (File.class) && !p.isAssignableFrom (Date.class)) {
				throw new Exception ("Can't reflect a function with " + p.getSimpleName () + " parameter");
			}
		}

		ParameterNames annnames = method.getAnnotation (ParameterNames.class);
		ParameterDescriptions anndesc = method.getAnnotation (ParameterDescriptions.class);
		for (int i = 0; i < parameters.length; ++i) {
			Class<?> param = parameters[i];

			Parameter ishellparam = new Parameter (i);
			ishellparam.cls = param;
			if ((annnames != null) && (annnames.value ().length > i)) {
				ishellparam.name = annnames.value ()[i];
			}

			if ((anndesc != null) && (anndesc.value ().length > i)) {
				ishellparam.description = anndesc.value ()[i];
			}

			params.add (ishellparam);
		}

		for (Class<?> exclass:method.getExceptionTypes ()) {
			excetions.add (exclass);
		}

		rettype = new ObjectMap ();
		rettype.put ("cls", method.getReturnType ().getSimpleName ());
		Package pkg = method.getReturnType ().getPackage ();
		rettype.put ("drilldown", true);
		if (method.getReturnType ().isPrimitive ()) {
			rettype.put ("primitive", true);
		} else {
			rettype.put ("primitive", false);
			if (pkg != null) {
				rettype.put ("pkg", pkg.getName ());
				String vendor = method.getReturnType ().getPackage ().getImplementationTitle ();
				rettype.put ("drilldown", !method.getReturnType ().isPrimitive () && ((vendor == null) || "".equalsIgnoreCase (vendor)));
			} else {
				rettype.put ("pkg", "default");
			}
		}
		rettype.put ("array", method.getReturnType ().isArray ());
	}

	@Override
	public String toString () {
		String s = name;
		if (description != null)
			s += " - " + description;
		for (Parameter param:params) {
			s += "\n\t\t" + param.toString ();
		}
		return s;
	}

	public String toHTML () {
		String s = "<td><form action='" + name + "' method='get'>" + name + "<td>";
		for (Parameter param:params) {
			s += " " + param.toHTML ();
		}
		s += "<td><input type='submit' value='Call'>";
		if (description != null)
			s += "<td>" + description;
		s += "</form>";
		return s;
	}

	//
	// public String selfDoc () {
	// String s = "<s3>" + name + "</h3>";
	// boolean retval = method.getReturnType () == null;
	// if (retval)
	// s += "<span class='ownurl'></span>" + name + "/";
	//
	// if (retval)
	// s += "[<a href='" + name + ".doc'>Drill Down</a>]";
	// s += "<h4>";
	// if (!retval)
	// s += "No return value";
	// else
	// s += "Return Type " + method.getReturnType ().getSimpleName () + "' (" +
	// method.getReturnType ().getPackage ().getName () + ")";
	// s += "</h4>";
	// return s;
	// }

	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		json.put ("name", name);
		json.put ("descr", description);
		JSONItem arr = JSONItem.newArray ();
		json.put ("params", arr);
		for (Parameter param:params) {
			arr.put (param.toJSON ());
		}
		return json;
	}

	public String getHtmlPath () {
		String s = "";
		s += this.getName ();
		for (Parameter param:params) {
			s += "/&lt;" + param.getName () + "&gt;";
		}
		return s;
	}

	public String getUrlParams () {
		String s = "";
		boolean first = true;
		for (Parameter param:params) {
			if (!first) {
				s += "&";
			} else {
				s += "?";
				first = false;
			}
			s += param.getName () + "=&lt;" + param.getDescription () + "&gt;";
		}
		return s;
	}

	static public class Parameter {
		public Class<?> cls;
		public String description;
		public String name;
		private int index;

		public Parameter (int index) {
			this.index = index;
		}

		public Class<?> getCls () {
			return cls;
		}

		public String getDescription () {
			return description;
		}

		public String getName () {
			if (name == null) {
				return "arg" + index;
			}
			return name;
		}

		@Override
		public String toString () {
			String s = name + "\t(" + cls.getSimpleName () + ")";
			if (description != null)
				s += "\t" + description;
			return s;
		}

		public JSONItem toJSON () throws JSONValidationException {
			JSONItem json = JSONItem.newObject ();
			json.put ("type", cls.getSimpleName ());
			json.put ("name", name);
			json.put ("descr", description);
			return json;
		}

		public String toHTML () {
			String s = "<input type='text' name='" + name + "' placeholder='" + description + "' tooltip='"
					+ cls.getSimpleName () + "'>";
			return s;
		}
	}

	@Override
	public int compareTo (IShellMethod other) {
		return this.name.compareTo (other.name);
	}
}

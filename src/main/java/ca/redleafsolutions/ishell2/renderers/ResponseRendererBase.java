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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.redleafsolutions.ishell2.IShellMethod;
import ca.redleafsolutions.ishell2.IShellRequest;

abstract public class ResponseRendererBase implements Comparable<ResponseRendererBase> {
	protected double timing;

	public ResponseRendererBase (double timing) {
		this.timing = timing;
	}

	abstract public String toString (Object o, IShellRequest request);
	abstract public String toDetails (Object o, IShellRequest request);
	abstract public String toExceptionString (Throwable throwable, IShellRequest request);

	protected Collection<IShellMethod> getMethods (Object o) {
		List<IShellMethod> list = new LinkedList<> ();
		Method[] methods = o.getClass ().getMethods ();
		for (Method method: methods) {
			if (!method.getDeclaringClass ().equals (Object.class)) {
				try {
					list.add (new IShellMethod (method));
				} catch (Exception e) {
					// when can't create ishell method: ignore
				}
			}
		}

		Collections.sort (list);
		return list;
	}

	protected Map<String, Object> getFields (Object o) {
		Field[] fields = o.getClass ().getFields ();
		Map<String, Object> map = new TreeMap<String, Object> ();
		for (Field field: fields) {
			try {
				map.put (field.getName (), field.get (o));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace ();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace ();
			}
		}
		return map;
	}

	@Override
	public int compareTo (ResponseRendererBase other) {
		String name1 = this.getClass ().getSimpleName ();
		String name2 = other.getClass ().getSimpleName ();
		return name1.compareTo (name2);
	}
}

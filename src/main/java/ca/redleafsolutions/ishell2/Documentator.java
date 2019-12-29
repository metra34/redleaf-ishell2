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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;

@Deprecated
public class Documentator {
	@MethodDescription ("Document the API of a class")
	@ParameterNames ("ishellpath")
	@ParameterDescriptions ("the path that is used in iShell to get this object")
	public Object api (String ishellpath) throws IShellException {
		return new Documentator.API (getIShellObject (ishellpath));
	}

	@MethodDescription ("Document the a class")
	@ParameterNames ("ishellpath")
	@ParameterDescriptions ("the path that is used in iShell to get this object")
	public ClassDoc cls (String ishellpath) throws IShellException {
		return new ClassDoc (getIShellObject (ishellpath));
	}

	private Object getIShellObject (String ishellpath) throws IShellException {
		ParseRequestResults parsed = new ParseRequestResults (ishellpath);
		IShellRequestSingle request = new IShellRequestSingle (parsed);
		IShellObject result = iShell.getInstance ().execute (request);
		if (result instanceof IShellObject.RawObject) {
		} else if (result instanceof IShellObject.ExecutedObject) {
		} else if (result instanceof IShellObject.ExceptionObject) {
			throw new IShellException (result.getObject ().toString ());
		}
		return result.getObject ();
	}

	static public class API {
		private Object o;

		public API (Object object) {
			this.o = object;
		}
		
		@Override
		public String toString () {
			String s = o.getClass ().getSimpleName () + " (" + o.getClass ().getPackage ().getName () + ")";
			for (Field field:o.getClass ().getFields ()) {
				s += "\nF " + field.toString () + " (" + field.isAccessible () + ", " + field.isEnumConstant () + ", " + field.isSynthetic () + ")";
			}
			for (Method method:o.getClass ().getMethods ()) {
				s += "\nM " + method.getModifiers () + ": " + method.toString () + " (" + method.isAccessible () + ", " + method.isBridge () + ", " + method.isSynthetic () + ", " + method.isVarArgs () + ")";
			}
			return s;
		}
	}

	public class ClassDoc {
		public ClassDoc (Object object) {
			// TODO Auto-generated constructor stub
		}
	}
}

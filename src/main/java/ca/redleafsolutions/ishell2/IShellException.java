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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;

@SuppressWarnings ("serial")
public class IShellException extends Exception {
	public static class AuthenticationError extends IShellException {

	}

	public IShellException () {
		super ();
	}

	public IShellException (String message) {
		super (message);
	}

	static public class ExtensionNotFound extends IShellException {
		private IShellRequest request;

		public ExtensionNotFound (IShellRequest request) {
			this.request = request;
		}

		public IShellRequest getRequest () {
			return request;
		}
	}

	static public class ResourceNotFound extends IShellException {
		private Object resource;

		public ResourceNotFound (Object res) {
			this.resource = res;
		}

		public Object getResource () {
			return resource;
		}

		@Override
		public String getMessage () {
			return resource + " was not found.";
		}
	}
	
	public static class AccessRestricted extends IShellException {
		private IShellRequest resource;

		public AccessRestricted (IShellRequest resource) {
			this.resource = resource;
		}

		public Object getResource () {
			return resource;
		}
	}

	static public class InvocationTargetException extends IShellException {
		private IShellRequest request;

		private Throwable exception;

		public InvocationTargetException (IShellRequest request, Throwable e) {
			this (e);
			this.request = request;
		}

		public InvocationTargetException (Throwable e) {
			this.exception = e;
		}

		public IShellRequest getRequest () {
			return request;
		}

		public Throwable getException () {
			return exception;
		}
		
		@Override
		public String toString () {
			return this.getClass ().getName () + " -> (" + exception.toString () + ")";
		}
		
		@Override
		public void printStackTrace () {
			exception.printStackTrace ();
		}
		
		@Override
		public void printStackTrace (PrintStream s) {
			exception.printStackTrace (s);
		}
		
		@Override
		public void printStackTrace (PrintWriter s) {
			exception.printStackTrace (s);
		}
	}

	static public class BadParameters extends IShellException {
		public BadParameters (Map<Integer, Method> methods) {
			super (constructMessage (methods));
		}

		static private String constructMessage (Map<Integer, Method> methods) {
			String s = "";
			if (methods.size () > 1) {
				s = "Use one of the following:";
			} else {
				s = "Use the following format:";
			}
			for (Method method: methods.values ()) {
				s += "\n\t" + method.getName () + " ";
				for (Class<?> p: method.getParameterTypes ()) {
					s += p.getSimpleName () + " ";
				}
			}
			return s;
		}
	}

	public static class KeyNotFound extends IShellException {
		private String key;
		private String[] options;

		public KeyNotFound (String name) {
			this (name, (String)null);
		}

		public KeyNotFound (String name, String... options) {
			super ("Key '" + name + "' was not found");
			this.key = name;
			this.options = options;
		}
		
		public KeyNotFound(String name, Set<String> options) {
			super ("Key '" + name + "' was not found");
			this.key = name;
			String[] list = new String[options.size()];
			int i = 0;
			for (String option:options) {
				list[i++] = option;
			}
			this.options = list;
		}

		public String getKey () {
			return key;
		}
		
		@Override
		public String toString () {
			return "Key " + key + " was not found." + (options != null? " Use only one of " + Arrays.toString (options) + ".": "");
		}
	}
	
	public static class IllegalFunctionOverload extends Exception {
		public IllegalFunctionOverload (Method method) {
			super ("Method " + method.getName () + " is ambigous. Make some " + IShellInvisible.class.getSimpleName ());
		}
	}
	
	public static class AlreadyExists extends IShellException {
		public AlreadyExists () {
		}

		public AlreadyExists (String message) {
			super (message);
		}
	}

	static public class NotImplementedYet extends RuntimeException {
	}
}

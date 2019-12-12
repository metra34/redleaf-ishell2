/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.interfaces;

import java.util.HashSet;
import java.util.Set;

import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequestScript;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.IShellResponse;
import ca.redleafsolutions.ishell2.ParseRequestResults;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.renderers.RendererFactory;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public abstract class IShellInterfaceImpl implements IShellInterface {
	protected iShell main;
	protected String defaultOutput;
	protected boolean timing;
	public Set<String> mapping;
//	private JSONValidator validator;
	private Set<IShellInterfaceHandler> handlers;

	public IShellInterfaceImpl (iShell main, JSONItem params) throws JSONValidationException {
		this.main = main;
		this.handlers = new HashSet<IShellInterfaceHandler> ();
//
//		validator = new JSONValidator ();
//		validator.put ("map", new ArrayValidator<String> ());
//		validator.put ("showtiming", new BooleanValidator ().optional (true));
//		validator.put ("default-format", new OptionValidator<String> (RendererFactory.getInstance ()
//				.listTypes ()).optional (true));
//
//		validator.validate (params);

		try {
			JSONItem maparr = params.getJSON ("map");
			this.mapping = new HashSet<> ();
			for (int i = 0; i < maparr.length (); ++i) {
				this.mapping.add (maparr.getString (i));
			}
		} catch (JSONValidationException e) {
			this.mapping = null;
		}

		try {
			timing = params.getBoolean ("showtiming");
		} catch (JSONValidationException e) {
			timing = false;
		}

		try {
			defaultOutput = params.getString ("default-format");
		} catch (JSONValidationException e) {
			this.defaultOutput = getNativeFormat ();
		}
		try {
			this.defaultOutput = params.getString ("default-format");
			if (RendererFactory.getInstance ().typeExists (defaultOutput)) {
				// legal format name: do nothing
			} else {
				this.defaultOutput = getNativeFormat ();
			}
		} catch (JSONValidationException e) {
			this.defaultOutput = getNativeFormat ();
		}
	}

	protected IShellObject execute (IShellRequestSingle request) throws IShellException {
		long tic = System.nanoTime ();
		if ((mapping != null) && !mapping.contains (request.getPath ().get (0))) {
			throw new IShellException.AccessRestricted (request);
		}

		IShellObject result = null;
		try {
			for (IShellInterfaceHandler handler:handlers) {
				if (handler.isOwnRequest (request)) {
					result = handler.execute (request);
					break;
				}
			}
		} catch (IShellException e) {
		}
		
		if (result == null)
			result = main.execute (request);
		result.setDuration (System.nanoTime () - tic);

		if (request.isDetails ()) {
			result.showDetails ();
		}
		return result;
	}

	public String defaultoutput () {
		return defaultOutput;
	}

	public void defaultoutput (String format) {
		defaultOutput = format;
	}

	public boolean timing () {
		return timing;
	}

	public void timing (boolean timing) {
		this.timing = timing;
	}

	abstract protected String getNativeFormat ();

	@Override
	public String toString () {
		return info ();
	}

	abstract public String info ();

	@Override
	public Set<IShellInterfaceHandler> getHandlers () {
		return handlers;
	}
	
	@Override
	public void addHandler (IShellInterfaceHandler handler) {
		handlers.add (handler);
	}
	
	@Override
	public void removeHandler (IShellInterfaceHandler handler) {
		handlers.remove (handler);
	}

	public IShellResponse executeAndRespond (IShellRequestSingle request, ParseRequestResults parsed) {
		return executeAndRespond(request, parsed.getOutputFormat (defaultoutput ()));
	}

	public IShellResponse executeAndRespond (IShellRequestSingle request, String outputformat) {
		long tic = System.nanoTime ();
		try {
			IShellObject executed = execute (request);
			return new IShellResponse (executed, outputformat, timing ? (System.nanoTime () - tic) / 1000000. : -1);
		} catch (IShellException e) {
			return new IShellResponse (new IShellObject.ExceptionObject (e, request), outputformat, timing ? (System.nanoTime () - tic) / 1000000. : -1);
		}
	}

	public IShellResponse executeAndRespond (IShellRequestScript request) {
		long tic = System.nanoTime ();
		try {
			IShellObject result = null;
			try {
				for (IShellInterfaceHandler handler:handlers) {
					if (handler.isOwnRequest (request)) {
						result = handler.execute (request);
						break;
					}
				}
			} catch (IShellException e) {
			}
			
			if (result == null)
				result = main.execute (request);
			result.setDuration (System.nanoTime () - tic);

			if (request.isDetails ()) {
				result.showDetails ();
			}

			return new IShellResponse (result, "text",
					timing ? (System.nanoTime () - tic) / 1000000. : -1);
		} catch (IShellException e) {
			return new IShellResponse (new IShellObject.ExceptionObject (e, request), "text",
					timing ? (System.nanoTime () - tic) / 1000000. : -1);
		}
	}
}

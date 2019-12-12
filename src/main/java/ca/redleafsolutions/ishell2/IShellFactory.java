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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ca.redleafsolutions.Instantiator;
import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.ishell2.IShellException.AlreadyExists;
import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.displays.Browser;
import ca.redleafsolutions.ishell2.displays.HTADisplay;
import ca.redleafsolutions.ishell2.displays.IShellDisplay;
import ca.redleafsolutions.ishell2.engines.IShellEngine;
import ca.redleafsolutions.ishell2.engines.SimpleEngine;
import ca.redleafsolutions.ishell2.interfaces.ConsoleInterface;
import ca.redleafsolutions.ishell2.interfaces.IShellHTTPInterface;
import ca.redleafsolutions.ishell2.interfaces.IShellInterface;
import ca.redleafsolutions.ishell2.interfaces.IShellTCPInterface;
import ca.redleafsolutions.ishell2.interfaces.http.InternalHTTPServer;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererBase;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererHTML;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererHTMLDoc;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererJSON;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererText;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererXML;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellFactory {
	static private IShellFactory instance = null;

	static public IShellFactory getInstance () {
		return instance;
	}

	static public IShellFactory create () throws SingletonException {
		if (instance != null)
			throw new SingletonException (instance);
		return instance = new IShellFactory ();
	}

	public IShellEngine createEngine (JSONItem json) {
		try {
			JSONItem engineobj = json.getJSON ("engine");
			try {
				IShellEngine engine;
				if (engineobj.has ("params")) {
					engine = new Instantiator<IShellEngine> ().instantiate (engineobj.getString ("class"), engineobj.getJSON ("params"));
				} else {
					engine = new Instantiator<IShellEngine> ().instantiate (engineobj.getString ("class"));
				}
					
				try {
					String redirect = engineobj.getString ("redirect");
					engine.setDefaultRedirect (redirect);
				} catch (JSONValidationException.MissingKey e) {
				}
				return engine;
			} catch (InstantiationException e) {
				iLogger.severe ("Failed to instantiate engine class " + engineobj + " due to " + e.toString ()
						+ ". Instantiating default engine");
			}
		} catch (JSONValidationException e) {
		}
		return new SimpleEngine ();
	}

	public List<IShellInterface> setInterfaces (iShell main, JSONItem jsonarr) throws IOException,
			ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONValidationException, SingletonException {
		List<IShellInterface> ifaces = new LinkedList<IShellInterface> ();
		for (int i = 0; i < jsonarr.length (); ++i) {
			String type = null;
			JSONItem json = null;
			try {
				json = jsonarr.getJSON (i);
				IShellInterface iface = null;
				try {
					type = json.getString ("type");
					switch (type) {
					case "http":
						iface = new InternalHTTPServer (main, json);
						break;
					case "console":
						iface = new ConsoleInterface (main, json);
						break;
					case "tcp":
						iface = new IShellTCPInterface (main, json);
						break;
					}
				} catch (JSONValidationException e) {
				}

				if (iface == null) {
					try {
						type = json.getString ("class");
						Class<?> cls = Class.forName (type);
						Constructor<?> ctor = cls.getConstructor (iShell.class, JSONItem.class);
						iface = (IShellInterface)ctor.newInstance (main, json);
					} catch (JSONValidationException e) {
					}
				}
				
				if (iface != null) {
					ifaces.add (iface);
					
					if (iface.isBrowserInterface ()) {
						main.setBrowserInterface ((IShellHTTPInterface)iface);
					}
				}
			} catch (JSONValidationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace ();
			}

		}
		return ifaces;
	}

	public ArrayList<IShellDisplay> createDisplays (JSONItem jsonarr) throws KeyNotFound, ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, JSONValidationException {
//		JSONValidator validator = new JSONValidator ();
//		validator.put ("type", new StringValidator ().optional (false));
//		validator.put ("url", new StringValidator ().optional (false));
//		validator.put ("params", new ClassValidator ().optional (true));
		jsonarr = JSONItem.forceJSONArray(jsonarr);
//		jsonarr = JSONUtils.forceJSONArray (jsonarr);
		ArrayList<IShellDisplay> displays = new ArrayList<> ();
		for (int i = 0; i < jsonarr.length (); ++i) {
			String type = null;
			String url = null;
			JSONItem params = null;
			JSONItem json = null;
			try {
				json = jsonarr.getJSON (i);
//				validator.validate (json);
				type = json.getString ("type");
				url = json.getString ("url");
				try {
					params = json.getJSON ("params");
				} catch (JSONValidationException e) {
				}
			} catch (JSONValidationException e) {
			}

			Class<? extends IShellDisplay> cls = getDisplayClass (type);
			// if ("hta".equalsIgnoreCase (type)) {
			// cls = HTADisplay.class;
			// } else if ("browser".equalsIgnoreCase (type)) {
			// cls = Browser.class;
			// } else {
			// cls = Class.forName (type);
			// }
			//
			// if (!IShellDisplays.class.isAssignableFrom (cls))
			// throw new ClassCastException (type +
			// " does not implement IShellDisplays inteface");

			IShellDisplay d = createDisplay (cls, url, params);
			displays.add (d);
		}
		return displays;
	}

	public IShellDisplay createDisplay (Class<? extends IShellDisplay> cls, String url) throws InstantiationException,
			IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
			InvocationTargetException, JSONValidationException {
		return createDisplay (cls, url, null);
	}

	private IShellDisplay createDisplay (Class<? extends IShellDisplay> cls, String url, JSONItem params)
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException, JSONValidationException {
		IShellDisplay d;
		if (params == null) {
			@SuppressWarnings ("unchecked")
			Constructor<IShellDisplay> ctor = (Constructor<IShellDisplay>)cls.getConstructor ();
			d = ctor.newInstance ();
		} else {
			Constructor<? extends IShellDisplay> ctor = cls.getConstructor (JSONItem.class);
			d = ctor.newInstance (params);
		}
		try {
			d.open (URI.create (url));
		} catch (IOException e) {
			iLogger.severe (e);
		}
		return d;
	}

	@IShellInvisible
	@SuppressWarnings ("unchecked")
	public Class<? extends IShellDisplay> getDisplayClass (String type) throws ClassNotFoundException {
		Class<?> cls;
		if ("hta".equalsIgnoreCase (type)) {
			cls = HTADisplay.class;
		} else if ("browser".equalsIgnoreCase (type)) {
			cls = Browser.class;
		} else {
			cls = Class.forName (type);
		}

		if (!IShellDisplay.class.isAssignableFrom (cls))
			throw new ClassCastException (type + " does not implement IShellDisplays inteface");

		return (Class<? extends IShellDisplay>)cls;
	}

	public void createRenderers (iShell ishell) {
		try {
			ishell.addRenderer ("TEXT", new String[] { "text", "txt" }, ResponseRendererText.class);
		} catch (AlreadyExists e) {
			iLogger.severe (e);
		}
		try {
			ishell.addRenderer ("JSON", new String[] { "json" }, ResponseRendererJSON.class);
		} catch (AlreadyExists e) {
			iLogger.severe (e);
		}
		try {
			ishell.addRenderer ("HTML", new String[] { "html", "htm", "js", "css" }, ResponseRendererHTML.class);
		} catch (AlreadyExists e) {
			iLogger.severe (e);
		}
		try {
			ishell.addRenderer ("XML", new String[] { "xml" }, ResponseRendererXML.class);
		} catch (AlreadyExists e) {
			iLogger.severe (e);
		}
		try {
			ishell.addRenderer ("HTDOC", new String[] { "htmldoc", "htdoc", "doc" }, ResponseRendererHTMLDoc.class);
		} catch (AlreadyExists e) {
			iLogger.severe (e);
		}
	}

	@SuppressWarnings ("unchecked")
	public void createRenderers (iShell ishell, JSONItem json) throws IShellException, JSONValidationException,
			ClassNotFoundException {
//		JSONValidator validator = new JSONValidator ();
//		validator.put ("type", new StringValidator ().optional (false));
//		validator.put ("class", new StringValidator ().optional (false));
//		validator.put ("extentions", new ArrayValidator<String> ().optional (false));
		for (int i = 0; i < json.length (); ++i) {
			Object item;
			try {
				item = json.get (i);
			} catch (JSONValidationException e) {
				throw new JSONValidationException.MissingKey ("renderers list");
			}

//			if (item instanceof JSONObject) {
//				item = JSONUtils.json2item ((JSONObject)item);
//			}
//
			if (item instanceof JSONItem) {
				JSONItem jsonitem = (JSONItem)item;
//				validator.validate (jsonitem);
				JSONItem arr = jsonitem.getJSON ("extentions");
				String extentions[] = new String[arr.length ()];
				for (int j = 0; j < arr.length (); ++j) {
					extentions[j] = arr.getString (j);
				}
				String classname = jsonitem.getString ("class");
				Class<?> cls = Class.forName (classname);
				if (!ResponseRendererBase.class.isAssignableFrom (cls))
					throw new ClassCastException (classname + " does not implement IShellDisplays inteface");
				ishell.addRenderer (jsonitem.getString ("type"), extentions, (Class<? extends ResponseRendererBase>)cls);
			} else if (item instanceof String) {
				switch ((String)item) {
				case "TEXT":
					ishell.addRenderer ("TEXT", new String[] { "text", "txt" }, ResponseRendererText.class);
					break;
				case "JSON":
					ishell.addRenderer ("JSON", new String[] { "json" }, ResponseRendererJSON.class);
					break;
				case "HTML":
					ishell.addRenderer ("HTML", new String[] { "html", "htm", "js", "css" }, ResponseRendererHTML.class);
					break;
				case "XML":
					ishell.addRenderer ("XML", new String[] { "xml" }, ResponseRendererXML.class);
					break;
				case "HTDOC":
					ishell.addRenderer ("HTDOC", new String[] { "doc", "htmldoc", "htdoc" },
							ResponseRendererHTMLDoc.class);
					break;
				default:
					throw new IShellException ("Renderer type " + item + " is not supported");
				}
			}
		}
	}
}

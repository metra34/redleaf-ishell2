/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Redleaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.Trace;
import ca.redleafsolutions.base.events.EventDispatcher;
import ca.redleafsolutions.ishell2.IShellException.AlreadyExists;
import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.ishell2.displays.IShellDisplay;
import ca.redleafsolutions.ishell2.displays.IShellDisplayWrapper;
import ca.redleafsolutions.ishell2.engines.IShellEngine;
import ca.redleafsolutions.ishell2.engines.ScriptableIShellEngine;
import ca.redleafsolutions.ishell2.interfaces.IShellHTTPInterface;
import ca.redleafsolutions.ishell2.interfaces.IShellInterface;
import ca.redleafsolutions.ishell2.interfaces.IShellInterfaceHandler;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.ishell2.renderers.RendererFactory;
import ca.redleafsolutions.ishell2.renderers.ResponseRendererBase;
import ca.redleafsolutions.ishell2.scripts.Script;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

/** Main iShell 2.0 wrapper and execution engine. On invoke, it will read the
 * configuration file (default: ishell.json) and instantiate the different
 * engines, interfaces, extentions and fire up the displas Terminology:
 * <b>Engine</b> execution and scripting engine <b>Extention</b> extending and
 * connecting an instance of a class into the engine. This allows runtime access
 * to the instance and it's methods. <b>Interface</b> an input/output channel to
 * issue commands and get the results from the engine <b>Display</b> an optional
 * launcher of a display tool such as a browser */
public class iShell extends EventDispatcher<IShellEvent> {
	static public final String lineSeparater = System.getProperty ("line.separator");

	/** Singleton instance */
	static private iShell instance = null;
	private static File bootfile;
	private static ObjectMap arguments;

	/** Singleton access method
	 * 
	 * @return instance of iShell */
	@IShellInvisible
	static public iShell getInstance () {
		return instance;
	}

	/** Main access and launch point to the entire engine and it's extentions
	 * 
	 * @param args standard Java runtime launch arguments */
	public static void main (String[] args) {
		arguments = new ObjectMap ();
		arguments.put ("target", "prod");
		
		int index = 0;
		while (index < args.length) {
			if (args[index].startsWith ("-")) {
				String key = args[index].substring (1);
				String value = args[++index];
				arguments.put (key.toLowerCase (), value);
			} else {
				bootfile = new File (args[index]);
			}
			++index;
		}

		if (bootfile == null) {
			bootfile = new File ("ishell.json");
		}

		try {
			instance = new iShell ();
			JSONItem json = instance.boot ();

			try {
				instance.initialize (json);
			} catch (Throwable e) {
				e.printStackTrace ();
				System.exit (0);
			}
		} catch (IOException | SingletonException | JSONValidationException e) {
			e.printStackTrace ();
			System.exit (0);
		}
	}

	private List<IShellInterface> ifaces = null;
	private ArrayList<IShellDisplay> displays = null;
	private IShellEngine engine = null;
	private List<IShellInterfaceHandler> handlers;
	private IShellDisplayWrapper displaywrapper;
	private IShellHTTPInterface browserinterface;

	// private Cache cache;

	private iShell () throws SingletonException {
//		try {
//			IShellVersion.register (this.getClass (), "iShell 2.0", "$Tag: $");
//		} catch (AlreadyExists e) {
//			throw new SingletonException (this);
//		}
	}

	private void initialize (JSONItem json) throws IOException, ClassNotFoundException, ClassCastException,
			InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException, JSONValidationException, SingletonException {

		handlers = new LinkedList<IShellInterfaceHandler> ();

		IShellFactory factory = IShellFactory.create ();

		this.engine = factory.createEngine (json);

		try {
			this.engine.extend (json.getJSON ("extend"));
		} catch (JSONValidationException e2) {
			// if no "extend" defined: do nothing
		}
		//
		// try {
		// cache = new Cache (json.getJSONObject ("cache"));
		// } catch (JSONException e) {
		// cache = new Cache ("cache");
		// }

		try {
			ifaces = factory.setInterfaces (this, json.getJSON ("interface"));
			for (IShellInterface iface:ifaces) {
				if (iface instanceof IShellHTTPInterface) {
					for (IShellInterfaceHandler handler:handlers) {
						iface.addHandler (handler);
					}
				}
			}
			handlers.clear ();
		} catch (JSONValidationException e) {
			ifaces = new LinkedList<> ();
		}

		try {
			displays = factory.createDisplays (json.getJSON ("display"));
		} catch (JSONValidationException e1) {
			displays = new ArrayList<> ();
		} catch (KeyNotFound e) {
			iLogger.severe (e);
		}

		displaywrapper = new IShellDisplayWrapper (displays);

		engine.extend ("ishell", this);
		if (engine instanceof ScriptableIShellEngine) {
			engine.extend ("script", new Script (engine));
		}
		engine.extend ("help", new Help (engine.extensions ()));

		try {
			try {
				factory.createRenderers (this, json.getJSON ("renderers"));
			} catch (IShellException e) {
				iLogger.severe (e);
			}
		} catch (JSONValidationException e) {
			factory.createRenderers (this);
		}
		dispatchEvent (new IShellEvent.ShellCreationComplete ());
	}

	public IShellObject execute (IShellRequest request) throws IShellException {
		IShellObject res = engine.execute (request);
		return res;
	}

	public List<IShellInterface> iface () {
		return this.ifaces;
	}

	public IShellEngine engine () {
		return this.engine;
	}

	public IShellDisplayWrapper display () {
		return displaywrapper;
	}

	public Map<String, Object> extensions () {
		return engine.extensions ();
	}
	
	public IShellHTTPInterface http () {
		return browserinterface;
	}

	public String bootfile () {
		return bootfile.toString ();
	}

	public JSONItem boot () throws JSONValidationException, IOException {
		Trace.on();Trace.info(bootfile.getAbsolutePath());
		InputStream is = new FileInputStream (bootfile);
		String s = TemplateUtils.evaluate (is, arguments);
		is.close ();
		return JSONItem.parse (s);
	}

	@IShellInvisible
	@Override
	public String toString () {
		return "root of " + this.getClass ().getSimpleName ();
	}

	public RendererFactory renderers () {
		return RendererFactory.getInstance ();
	}

	@IShellInvisible
	public void addRenderer (String type, String[] extentions, Class<? extends ResponseRendererBase> renderer)
			throws AlreadyExists {
		//		try {
		//			RendererFactory.getInstance ().getDefaultRenderer (0);
		//		} catch (Throwable e) {
		//			RendererFactory.getInstance ().registerDefault (type, extentions, renderer);
		//		}
		renderers ().register (type, extentions, renderer);
	}

	@IShellInvisible
	public void removeRenderer (String type) throws KeyNotFound {
		renderers ().unregister (type);
	}
//
//	@MethodDescription ("Check the (short) version IDs of the different iShell modules")
//	public ObjectMap2 ver () {
//		return IShellVersion.ver ();
//	}

	/** Add an external HTTP handler class
	 * 
	 * @param handler the handler */
	public void addHTTPHandler (IShellInterfaceHandler handler) {
		if ((ifaces != null) && (ifaces.size () > 0)) {
			for (IShellInterface iface:ifaces) {
				if (iface instanceof IShellHTTPInterface) {
					iface.addHandler (handler);
				}
			}
		} else {
			this.handlers.add (handler);
		}
	}

	@MethodDescription ("Get HTTP response")
	@ParameterNames ("url")
	@ParameterDescriptions ("The URL of resource to get")
	public IShellInputStream http (String url) throws MalformedURLException, IOException {
		if (url.indexOf ("://") < 0) {
			url = "http://" + url;
		}
		URLConnection conn = new URL (url).openConnection ();
		return new IShellInputStream (conn.getInputStream (), conn.getContentLength ());
	}

	@MethodDescription ("Get HTTP header")
	@ParameterNames ("url")
	@ParameterDescriptions ("The URL of resource to get headers")
	public Map<String, List<String>> httphead (String url) throws MalformedURLException, IOException {
		if (url.indexOf ("://") < 0) {
			url = "http://" + url;
		}
		URLConnection conn = new URL (url).openConnection ();
		Map<String, List<String>> headers = conn.getHeaderFields ();
		conn.getInputStream ().close ();
		return headers;
	}
	
	public static ObjectMap version () {
		ObjectMap map = new ObjectMap ();
		String cp = System.getProperty ("java.class.path");
		String[] cplst = cp.split (";");
		for (String cpitem:cplst) {
			File root = new File (cpitem);
			if (root.isDirectory ()) {
				File[] files = root.listFiles (new FilenameFilter () {
					@Override
					public boolean accept (File dir, String name) {
						return name.startsWith (".ver-");
					}
				});
				if (files != null) {
					for (File file:files) {
						try {
							byte[] bytes = Files.readAllBytes (Paths.get (file.toURI ()));
							map.put (file.getName ().substring (5), new String (bytes));
						} catch (IOException e) {
							map.put (file.getName (), e);
						}
					}
				}
			} else if (root.isFile ()) {
				try {
					@SuppressWarnings ("resource")
					JarFile jar = new JarFile (root);
					for (Enumeration<JarEntry> it = jar.entries (); it.hasMoreElements ();) {
						JarEntry entry = it.nextElement ();
						if (entry.getName ().startsWith (".") && (entry.getName ().indexOf ("/") < 0)) {
							try {
								InputStream is = jar.getInputStream (entry);
								int len = 0;
								byte[] buff = new byte[64];
								StringBuffer sb = new StringBuffer ();
								while ((len = is.read (buff)) > 0) {
									sb.append (new String (buff, 0, len));
								}
								is.close ();
								map.put (entry.getName ().substring (5), sb.toString ());
							} catch (IOException e) {
								map.put (entry.getName ().substring (5), e);
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace ();
				}

			}
		}
		return map;
	}

	public String getPrompt () {
		return engine.getPrompt ();
	}

	void setBrowserInterface (IShellHTTPInterface iface) {
		if (this.browserinterface == null)
			this.browserinterface = iface;
	}
}

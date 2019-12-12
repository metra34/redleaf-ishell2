package ca.redleafsolutions.ishell2.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellInputStream;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.ishell2.interfaces.IShellHTTPInterface;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.ishell2.ui.CacheException.NotFound;
import ca.redleafsolutions.ishell2.ui.notifications.NoticiationChannelBase;
import ca.redleafsolutions.ishell2.ui.notifications.NotificationChannelFactory;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadable;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class JSUI implements IShellUI {
	static private final String NOLIBKEY = "$#_NOLIB_#$";
	private ObjectMap libraries;
	private List<File> filesroot;
	//	private File cacheroot;
	private String ownextension;
	private Map<String, LinkedHashSet<JSLibrary>> includes;
	private Cache cache;
	private NoticiationChannelBase channel;

	public JSUI (JSONItem json) throws JSONValidationException, IOException, SingletonException {
		libraries = new ObjectMap ();
		includes = new TreeMap<> ();

		try {
			File file = new File (json.getString ("settings-file"));
			JSONItem settings = JSONItem.fromFile (file);
			for (Object skey:settings.listKeys ()) {
				String key = skey.toString ();
				libraries.put (key, new JSLibrary.JSON (key, settings.getJSON (key), ""));
			}
		} catch (JSONValidationException.MissingKey e) {
		}

		filesroot = new LinkedList<> ();
		try {
			filesroot.add (new File (json.getString ("root")));
		} catch (JSONValidationException.IllegalValue e) {
			JSONItem arr = json.getJSON ("root");
			if (arr.isObject ())
				throw new JSONValidationException.IllegalValue ("root", "Must be a String or Array of Strings");
			for (int i = 0; i < arr.length (); ++i) {
				filesroot.add (new File (arr.getString (i)));
			}
		}

		try {
			cache = new Cache (json.getJSON ("cache"));
		} catch (JSONValidationException.MissingKey e) {
			// ignore, can operate with no cache
		}

		ownextension = json.getString ("own-extension");
		ownextension = ownextension.trim ();
		
		try {
			JSONItem jnotify = json.getJSON ("notifications");
			new NotificationChannelFactory (jnotify);
			this.channel = NotificationChannelFactory.getInstance ();
		} catch (JSONValidationException e) {
		}
	}

	public NoticiationChannelBase channel () {
		return channel;
	}
	
	public Cache cache () {
		return cache;
	}
	
	public IShellInputStream jquery () throws IOException {
		return _file ("jquery.js");
	}
	public IShellInputStream bootstrapcss () throws IOException {
		return _file ("bootstrap.css");
	}
	public IShellInputStream bootstrapjs () throws IOException {
		return _file ("bootstrap.js");
	}
	public IShellInputStream angular () throws IOException {
		return _file ("angular.js");
	}
	public IShellInputStream fontawesome () throws IOException {
		return _file ("font-awesome.css");
	}
	
	private IShellInputStream _file (String filename) throws IOException {
		ResourceLocator locator = new ResourceLocator (JSUI.class);
		InputStream is = locator.getInputStream (filename);
		if (is == null) {
			throw new FileNotFoundException (filename);
		}
		return new IShellInputStream (is, is.available ());
	}

	public String parse (String pageid, String resourcename) throws FileNotFoundException {
		if ((cache != null) && cache.isEnabled ()) {
			try {
				return cache.fetch (pageid, resourcename);
			} catch (NotFound e) {
				// cached page was not found. Just proceed.
			} catch (IOException e) {
				iLogger.warning ("Cache failed to fetch resource " + resourcename + " on page " + pageid + " due to " + e);
			}
		}
		for (File root:filesroot) {
			File file = new File (root, resourcename);
			if (file.exists () && file.isFile ()) {
				InputStream is = null;
				try {
					is = new FileInputStream (file);
					ObjectMap map = IShellHTTPInterface.templateSetup (pageid);
					String result = TemplateUtils.evaluate (is, map);
					try {
						cache.store (pageid, resourcename, result);
					} catch (IOException e) {
						iLogger.warning ("Cache failed to store resource " + resourcename + " on page " + pageid + " due to " + e);
					}
					return result;
				} finally {
					if (is != null) {
						try {
							is.close ();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		throw new FileNotFoundException (resourcename);
	}

	public LinkedHashSet<JSLibrary> includes (String pageid) {
		return includes.get (pageid);
	}

	public void include (String pageid, Object liblist) {
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			libs = new LinkedHashSet<JSLibrary> ();
			includes.put (pageid, libs);
		}

		for (Object item:object2list (liblist)) {
			String libname = item.toString ();
			JSLibrary jslib = (JSLibrary)libraries.get (libname);
			if (jslib == null) {
				try {
					jslib = new JSLibrary.Files (libname, getLibRoot (libname), ownextension);
				} catch (FileNotFoundException e) {
					jslib = new JSLibrary.NotFound (libname, ownextension);
				}
			}
			if (!libs.contains (jslib))
				libs.add (jslib);
		}
	}

	@SuppressWarnings ("unchecked")
	private Iterable<String> object2list (Object liblist) {
		if (liblist instanceof Iterable<?>) {
			return (Iterable<String>)liblist;
		}
		return Arrays.asList (new String[] { (String)liblist });
	}

	public void includejs (String pageid, Object jslist) {
		JSLibrary jslib = getNoLibJSLibrary (pageid);
		for (String item:object2list (jslist)) {
			List<String> js = jslib.js ();
			if (!js.contains (item))
				js.add (item);
		}
	}

	public void includecss (String pageid, Object csslist) {
		JSLibrary jslib = getNoLibJSLibrary (pageid);
		for (String item:object2list (csslist)) {
			List<String> css = jslib.css ();
			if (!css.contains (item))
				jslib.css ().add (item);
		}
	}

	public void includehtml (String pageid, Object htmllist) {
		JSLibrary jslib = getNoLibJSLibrary (pageid);
		for (String item:object2list (htmllist)) {
			jslib.htmllist ().add (item);
		}
	}

	private JSLibrary getNoLibJSLibrary (String pageid) {
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			libs = new LinkedHashSet<JSLibrary> ();
			includes.put (pageid, libs);
		}

		JSLibrary jslib = null;
		for (JSLibrary jsl:libs) {
			if (NOLIBKEY.equals (jsl.getName ())) {
				jslib = jsl;
				break;
			}
		}
		if (jslib == null) {
			jslib = new JSLibrary.Lists (NOLIBKEY, this.ownextension);
			libs.add (jslib);
		}
		return jslib;
	}

	public ObjectMap libs () {
		return libraries;
	}

	public String html (String pageid, String libname) {
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			return "<!-- no includes found for page " + pageid + " -->";
		}
		StringBuffer sb = new StringBuffer ();
		for (JSLibrary jslib:libs) {
			if (libname.equalsIgnoreCase (jslib.getName ())) {
				for (String htmluri:jslib.htmllist ()) {
					URI uri = null;
					try {
						uri = new URI (htmluri);
					} catch (URISyntaxException e) {
					}
					if (uri != null) {
						if (uri.getScheme () == null) {
							uri = null;
						}
					}

					File file = null;
					if (uri == null) {
						try {
							file = new File (getLibRoot (libname), htmluri);
							uri = file.toURI ();
						} catch (FileNotFoundException e1) {
							sb.append ("<!-- Fail to show HTML content due to " + e1 + " -->");
						}
					}

					if (uri != null) {
						sb.append ("<!-- Starting include " + htmluri + " ------------->\n");
						InputStream is = null;
						try {
							ObjectMap map = IShellHTTPInterface.templateSetup (pageid);
							is = new FileInputStream (file);
							sb.append (TemplateUtils.evaluate (is, map));
							//							sb.append (new String (java.nio.file.Files.readAllBytes (Paths.get (uri))));
						} catch (IOException e) {
							sb.append ("<!-- Fail to show HTML content due to " + e + " -->");
						} finally {
							if (is != null) {
								try {
									is.close ();
								} catch (IOException e) {}
							}
						}
						sb.append ("<!-- End include " + htmluri + " ------------------>\n");
					}
				}
			}
		}
		return sb.toString ();
	}

	public List<String> csslist (String pageid) {
		List<String> list = new LinkedList<> ();
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			list.add ("<!-- no includes found for page " + pageid + " -->");
		} else {
			for (JSLibrary jslib:libs) {
				list.addAll (jslib.css ());
			}
		}
		return list;
	}

	public String csstags (String pageid) {
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			return "<!-- no libraries found for page " + pageid + " -->";
		}
		StringBuffer sb = new StringBuffer ();
		sb.append ("<!-- Starting CSS include ----------->\n");
		for (JSLibrary jslib:libs) {
			for (String file:jslib.css ()) {
				sb.append ("<link rel='stylesheet' type='text/css' href='" + file + "' />\n");
			}
		}
		sb.append ("<!-- End of CSS include ------------->\n");
		return sb.toString ();
	}

	public List<String> jslist (String pageid) {
		List<String> list = new LinkedList<> ();
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			list.add ("<!-- no includes found for page " + pageid + " -->");
		} else {
			for (JSLibrary jslib:libs) {
				list.addAll (jslib.js ());
			}
		}
		return list;
	}

	public String jstags (String pageid) {
		LinkedHashSet<JSLibrary> libs = includes.get (pageid);
		if (libs == null) {
			return "<!-- no libraries found for page " + pageid + " -->";
		}
		StringBuffer sb = new StringBuffer ();
		sb.append ("<!-- Starting JS include --------->\n");
		for (JSLibrary jslib:libs) {
			for (String file:jslib.js ()) {
				sb.append ("<script type='text/javascript' src='" + file + "'></script>\n");
			}
		}
		sb.append ("<!-- End of JS include ----------->\n");
		return sb.toString ();
	}

	@MethodDescription ("Get css and js files from repo")
	@ParameterNames ({ "lib", "path" })
	@ParameterDescriptions ({ "Library name", "Path to file" })
	public IShellInputStream files (String lib, String path) throws FileNotFoundException {
		for (File root:filesroot) {
			File libdir = new File (root, lib);
			File file = new File (libdir, path);
			if (file.exists () && file.isFile ())
				return new IShellInputStream (new FileInputStream (file), file.length ());
		}
		throw new FileNotFoundException (lib + "/" + path);
	}

	private File getLibRoot (String libname) throws FileNotFoundException {
		for (File dir:filesroot) {
			File file = new File (dir, libname);
			if (file.exists ())
				return file;
		}
		throw new FileNotFoundException (libname);
	}
	
	public String dateFormat (Object o, String pattern) {
		Date date;
		if (o instanceof Long) {
			date = new Date (((Long)o).longValue ());
		} else if (o instanceof Integer) {
			date = new Date (((Integer)o).longValue ());
		} else if (o instanceof Date) {
			date = (Date)o;
		} else {
			return o.toString ();
		}
		DateFormat df = new SimpleDateFormat (pattern);
		return df.format (date);
	}
	
	public Date dateParse (Object o, String pattern) throws ParseException {
		DateFormat df = new SimpleDateFormat (pattern);
		return df.parse (o.toString ());
	}
	//
	//	public JSUI2 refresh () {
	//		for (File rootdir:filesroot) {
	//			for (File libroot:rootdir.listFiles (new FileFilter () {
	//				@Override
	//				public boolean accept (File pathname) {
	//					return pathname.isDirectory ();
	//				}
	//			})) {
	//					JSLibrary jslib = new JSLibraryList (extractFiles (libroot, ".css"), extractFiles (libroot, ".js"), extractFiles (libroot, ".html"));
	//					libraries.put (libroot.getName ().toString (), jslib);
	//				}
	//			}
	//		return this;
	//	}
	//
	//	private String[] extractFiles (File root, final String ext) {
	//		String[] files = root.list (new FilenameFilter () {
	//			@Override
	//			public boolean accept (File dir, String name) {
	//				return name.toLowerCase ().endsWith (ext);
	//			}
	//		});
	//		for (int i = 0; i < files.length; ++i) {
	//			files[i] = ownpath + "files" + ext + "/?lib=" + root.getName () + "&path=" + files[i];
	//		}
	//		return files;
	//	}

	static public abstract class JSLibrary implements JSONWritable {
		protected String name;
		protected String ownextension;

		public abstract List<String> css ();

		public abstract List<String> js ();

		public abstract List<String> htmllist ();

		public abstract String html ();

		public JSLibrary (String name, String ownextension) {
			this.name = name;
			this.ownextension = ownextension;
		}

		protected String getRootPath () {
			return "/" + ownextension + "/files?lib=" + name + "&path=";
		}

		protected String getRootPath (String type) {
			return "/" + ownextension + "/files" + type + "?lib=" + name + "&path=";
		}

		public String getName () {
			return name;
		}

		public ObjectMap all () {
			ObjectMap map = new ObjectMap ();
			map.put ("css", css ());
			map.put ("js", js ());
			map.put ("html", html ());
			return map;
		}

		@Override
		public JSONItem toJSON () throws JSONValidationException {
			return all ().toJSON ();
		}

		@Override
		public String toString () {
			return name + ": css> " + css () + ", js> " + js () + ", html> " + html ();
		}

		@Override
		public boolean equals (Object other) {
			if (!(other instanceof JSLibrary))
				return false;
			return name.equals (((JSLibrary)other).getName ());
		}

		@Override
		public int hashCode () {
			return name.hashCode ();
		}

		static public class Files extends JSLibrary {
			private File root;

			public Files (String name, File root, String ownextension) {
				super (name, ownextension);
				this.root = root;
			}

			@Override
			public List<String> css () {
				List<String> list = new LinkedList<> ();
				for (String file:filesByExtension (".css")) {
					list.add (getRootPath (".css") + file);
				}
				return list;
			}

			@Override
			public List<String> js () {
				List<String> list = new LinkedList<> ();
				for (String file:filesByExtension (".js")) {
					list.add (getRootPath (".js") + file);
				}
				return list;
			}

			@Override
			public List<String> htmllist () {
				return filesByExtension (".html");
			}

			@Override
			public String html () {
				StringBuffer sb = new StringBuffer ();
				for (String item:filesByExtension (".html")) {
					sb.append ("<!-- Starting to include " + item + " ---------->\n");
					try {
						sb.append (java.nio.file.Files.readAllBytes (Paths.get (new File (item).toURI ())));
					} catch (IOException e) {
						sb.append ("<!-- Error read HTML data (" + e + ") -->");
					}
					sb.append ("<!-- End of " + item + " ----------------------->\n");
				}
				return sb.toString ();
			}

			private List<String> filesByExtension (final String ext) {
				String[] files = root.list (new FilenameFilter () {
					@Override
					public boolean accept (File dir, String name) {
						return name.toLowerCase ().endsWith (ext.toLowerCase ());
					}
				});
				Arrays.sort (files);
				return Arrays.asList (files);
			}
		}

		static public class JSON extends JSLibrary implements JSONReadable {
			private List<String> css;
			private List<String> js;
			private List<String> html;

			public JSON (String name, JSONItem json, String ownextension) throws JSONValidationException {
				super (name, ownextension);
				fromJSON (json);
			}

			@Override
			public List<String> css () {
				return css;
			}

			@Override
			public List<String> js () {
				return js;
			}

			@Override
			public List<String> htmllist () {
				return html;
			}

			@Override
			public String html () {
				StringBuffer sb = new StringBuffer ();
				for (String item:html) {
					sb.append ("<!-- Starting to include " + item + " ---------->\n");
					try {
						sb.append (java.nio.file.Files.readAllBytes (Paths.get (new URI (item))));
					} catch (IOException | URISyntaxException e) {
						sb.append ("<!-- Error read HTML data (" + e + ") -->");
					}
					sb.append ("<!-- End include " + item + " ------------------>\n");
				}
				return sb.toString ();
			}

			@Override
			public void fromJSON (JSONItem json) throws JSONValidationException {
				css = json2list (json, "css");
				js = json2list (json, "js");
				html = json2list (json, "html");
			}

			private List<String> json2list (JSONItem json, String key) throws JSONValidationException {
				List<String> list = new LinkedList<> ();
				JSONItem arr = null;
				try {
					arr = json.getJSON (key);
					arr = JSONItem.forceJSONArray (arr);
				} catch (JSONValidationException.IllegalValue e) {
					arr = JSONItem.newArray ();
					arr.put (json.getString (key));
				} catch (JSONValidationException.MissingKey e) {
					return list;
				}
				for (int i = 0; i < arr.length (); ++i) {
					String item = arr.getString (i);
					list.add (item);
				}
				return list;
			}
		}

		static public class Lists extends JSLibrary {
			private List<String> css;
			private List<String> js;
			private List<String> html;

			public Lists (String name, String ownextension) {
				super (name, ownextension);
				css = new LinkedList<> ();
				js = new LinkedList<> ();
				html = new LinkedList<> ();
			}

			public Lists (String name, String ownextension, String[] cssfiles, String[] jsfiles, String[] htmlfiles) {
				super (name, ownextension);
				for (int i = 0; i < cssfiles.length; ++i) {
					cssfiles[i] = getRootPath (".css") + cssfiles[i];
				}
				css = Arrays.asList (cssfiles);
				for (int i = 0; i < jsfiles.length; ++i) {
					jsfiles[i] = getRootPath (".css") + jsfiles[i];
				}
				js = Arrays.asList (jsfiles);
				html = Arrays.asList (htmlfiles);
			}

			@Override
			public List<String> css () {
				return css;
			}

			@Override
			public List<String> js () {
				return js;
			}

			@Override
			public List<String> htmllist () {
				return html;
			}

			@Override
			public String html () {
				StringBuffer sb = new StringBuffer ();
				for (String item:html) {
					sb.append ("<!-- Starting to include " + item + " ---------->\n");
					try {
						sb.append (java.nio.file.Files.readAllBytes (Paths.get (new URI (item))));
					} catch (IOException | URISyntaxException e) {
						sb.append ("<!-- Error read HTML data (" + e + ") -->");
					}
					sb.append ("<!-- End include " + item + " ------------------>\n");
				}
				return sb.toString ();
			}
		}

		static public class NotFound extends JSLibrary {
			public NotFound (String name, String ownextension) {
				super (name, ownextension);
			}

			@Override
			public List<String> css () {
				return Arrays.asList (new String[] { "<!-- Library " + super.name + " not found -->" });
			}

			@Override
			public List<String> js () {
				return Arrays.asList (new String[] { "<!-- Library " + super.name + " not found -->" });
			}

			@Override
			public List<String> htmllist () {
				return Arrays.asList (new String[] { "<!-- Library " + super.name + " not found -->" });
			}

			@Override
			public String html () {
				return "<!-- Library " + super.name + " not found -->";
			}
		}
	}
}

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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;
import ca.redleafsolutions.ishell2.renderers.RendererFactory;

public class ParseRequestResults {
	private List<String> path;
	private ObjectMap params;
	private String outputFormat;
	private String pathString;

	//
	// public ParseRequestResults (List<String> path, Map<String, String>
	// params, String outputFormat) {
	// this.path = path;
	// this.params = params;
	// this.outputFormat = outputFormat;
	// }

	public List<String> getPath () {
		return path;
	}

	public ObjectMap getParams () {
		return params;
	}

	public Object getParam (String key) throws KeyNotFound {
		if (params.containsKey (key))
			return params.get (key);
		throw new KeyNotFound (key);
	}

	public String getOutputFormat (String defaultOutput) {
		if (outputFormat == null) {
			String lastElement = path.get (path.size () - 1);
			int dot = lastElement.lastIndexOf (".");
			if (dot > 0) {
				return lastElement.substring (dot + 1);
			}
		}

		if (outputFormat == null)
			return defaultOutput;
		if ("".equals (outputFormat))
			return defaultOutput;
		return outputFormat;
	}

	public void setFormat (String format) {
		this.outputFormat = format;
	}

	public ParseRequestResults () {
		this.path = new LinkedList<String> ();
		this.params = new ObjectMap ();
		this.outputFormat = null;
	}

	public ParseRequestResults (URI uri) {
		this ();
		init (uri);
	}

	private void init (URI uri) {
		String s = uri.getPath ();
		try {
			s = URLDecoder.decode (s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		pathString = s;
		// pathString = s.toLowerCase (); in Linux systems this changes path case. In Windows it is ignored
		if (s.indexOf ("/") == 0)
			s = s.substring (1);
		String[] lst = s.split ("/");
		String last = lst[lst.length - 1];
		int pos = last.lastIndexOf ('.');
		if (pos >= 0) {
			this.outputFormat = last.substring (pos + 1);
			if (RendererFactory.getInstance ().extentionExists (outputFormat)) {
				last = last.substring (0, pos);
				lst[lst.length - 1] = last;
			} else {
				outputFormat = null;
			}
		}

		this.path = Arrays.asList (lst);

		s = uri.getRawQuery ();
		if (s != null) {
			if (s.indexOf ("?") == 0)
				s = s.substring (1);
			lst = s.split ("&");
			for (String kv:lst) {
				pos = kv.indexOf ('=');
				String key;
				String value;
				if (pos > 0) {
					key = kv.substring (0, pos);
					value = kv.substring (pos + 1);
					try {
						value = URLDecoder.decode (value, "UTF-8");
					} catch (UnsupportedEncodingException e) {
					}
				} else {
					key = kv;
					value = "";
				}
				this.params.put (key, value);
			}
		}
	}

	public ParseRequestResults (URI uri, String key) {
		this (uri);
		if (pathString.startsWith ("/" + key)) {
			pathString = pathString.replace ("/" + key, "");
		}
		if (pathString.indexOf ('/') == 0)
			pathString = pathString.substring (1);
	}

	public ParseRequestResults (String cmdline) {
		this ();

		String s = cmdline.trim ();
		pathString = s.toLowerCase ();
		String sep = "/";
		int posslash = s.indexOf ('/');
		int posspace = s.indexOf (' ');
		if ((posspace > 0) && ((posslash < 0) || (posspace < posslash))) {
			sep = " ";
		}

		if ("/".equals (sep)) {
			try {
				init (new URI (s));
			} catch (URISyntaxException e) {
				init (s, sep);
			}
		} else {
			init (s, sep);
		}
	}

	private void init (String cmdline, String sep) {
		String[] lst = cmdline.split (sep);
		String last = lst[lst.length - 1];
		int pos = last.lastIndexOf ('.');
		if (pos >= 0) {
			this.outputFormat = last.substring (pos + 1);
			if (RendererFactory.getInstance ().extentionExists (outputFormat)) {
				last = last.substring (0, pos);
				lst[lst.length - 1] = last;
			} else {
				outputFormat = null;
			}
		}

		this.path = new ArrayList<> ();
		for (String s2:lst) {
			s2 = s2.trim ();
			if (!s2.equals ("")) {
				try {
					path.add (URLDecoder.decode (s2, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					path.add (s2);
				}
			}
		}
	}

	public File file (File root) {
		String spath = "";
		for (String p:path) {
			if (spath.length () <= 0)
				spath += File.separator;
			spath += p;
		}
		if ((outputFormat != null) && (outputFormat.length () > 0))
			spath += "." + outputFormat;
		return new File (root, spath);
	}

	public String getPathString () {
		return pathString;
	}

	public boolean hasFormatExtension () {
		return outputFormat != null;
	}

	public void addFiles (Map<String, File> files) {
		params.putAll (files);
	}

	public void addParams (Map<String, Object> params) {
		this.params.putAll (params);
	}
}

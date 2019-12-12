/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ca.redleafsolutions.Trace;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellException.ResourceNotFound;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class NGinX {
	private File path;
	private String configfile;
	private JSONItem json;

	public NGinX (JSONItem config) throws FileNotFoundException, JSONValidationException {
		String s = this.getClass ().getPackage ().getName ().replaceAll ("[.]", "/") + "/nginx.defaults.js";
		JSONItem defaults;
		try {
			defaults = JSONItem.parse (resourceToString (findResource (s)));
		} catch (IOException | ResourceNotFound e) {
			defaults = JSONItem.newObject ();
		}
		defaults.put ("config", this.getClass ().getPackage ().getName ().replaceAll ("[.]", "/") + "/nginx.conf");

		json = applyDefaluts (config, defaults);
		path = new File (json.getString ("path"));
		if (!path.exists ())
			throw new FileNotFoundException (path.getAbsolutePath ());

		configfile = json.getString ("config");
	}

	private JSONItem applyDefaluts (JSONItem config, JSONItem defaults) throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		@SuppressWarnings ("unchecked")
		Iterator<String> it = (Iterator<String>)defaults.keys ();
		while (it.hasNext ()) {
			String key = it.next ();
			Object value = defaults.get (key);
			if (value instanceof JSONItem) {
				json.put (key, applyDefaluts (config.getJSON (key), (JSONItem)value));
			} else {
				if (config.has (key)) {
					json.put (key, config.get (key));
				} else {
					json.put (key, defaults.get (key));
				}
			}
		}
		return json;
	}

	public JSONItem params () throws JSONValidationException {
		return json;
	}

	public String conf () throws IOException, ResourceNotFound {
		Velocity.init ();
		VelocityContext context = new VelocityContext ();
		context.put ("conf", json);
		context.put ("extentions", iShell.getInstance ().extensions ().keySet ());
		Writer writer = new StringWriter ();
		String logTag = "";
		StringReader reader;
		reader = new StringReader (resourceToString (findResource (configfile)));
		try {
			if (Velocity.evaluate (context, writer, logTag, reader)) {
				// remove all comment lines from output
				String s = "";
				for (String line:writer.toString ().split ("\n")) {
					String trimline = line.trim ();
					if ((trimline.length () > 0) && (trimline.indexOf ("#") != 0)) {
						s += line;
					}
				}
				return s;
			}
		} finally {
			reader.close ();
		}
		return null;
	}

	public List<File> extract () throws MalformedURLException, IOException, ResourceNotFound {
		List<File> updated = new LinkedList<> ();
		URI uri = findResource ("native/win/nginx.zip");
		if (uri != null) {
			File destination;
			try {
				destination = new File (json.getString ("path"));
			} catch (JSONValidationException e) {
				destination = new File ("nginx");
			}
			if (!destination.exists ()) {
				destination.mkdirs ();
			} else if (!destination.isDirectory ()) {
				throw new FileSystemException ("Destination directory " + destination + " can not be created");
			}

			ZipInputStream zip = new ZipInputStream (uri.toURL ().openStream ());
			while (true) {
				ZipEntry e = zip.getNextEntry ();
				if (e == null)
					break;
				String name = new File (e.getName ()).toString ();
				File fsfile = new File (destination, name);
				Trace.info (name);
				if (e.isDirectory ()) {
					if (fsfile.exists ()) {
						if (!fsfile.isDirectory ()) {
							throw new FileSystemException ("Mismatched file system @ " + fsfile);
						}
					} else {
						updated.add (fsfile);
						fsfile.mkdir ();
					}
				} else {
					long size = e.getSize ();
					if (!fsfile.exists ()) {
						updated.add (fsfile);
						extractFile (zip, e, fsfile);
					} else {
						if (size != fsfile.length ()) {
							updated.add (fsfile);
							extractFile (zip, e, fsfile);
						}
					}
				}
			}

			// update configuration file
			FileWriter os = new FileWriter (new File (destination, "conf/nginx.conf"));
			os.write (conf ());
			os.close ();
		}
		return updated;
	}

	private void extractFile (ZipInputStream zip, ZipEntry e, File fsfile) throws IOException {
		byte[] buffer = new byte[4096];
		FileOutputStream output = null;
		try {
			output = new FileOutputStream (fsfile);
			int len = 0;
			while ((len = zip.read (buffer)) > 0) {
				output.write (buffer, 0, len);
			}
		} finally {
			// we must always close the output file
			if (output != null)
				output.close ();
		}
	}

	public void start () {
		int pos = path.getAbsolutePath ().indexOf (":");
		String script = path.getAbsolutePath ().substring (0, pos) + ":" + System.lineSeparator ();
		script += "CD \"" + path.getAbsolutePath () + "\"" + System.lineSeparator ();
		script += "start nginx.exe" + System.lineSeparator ();
		script += "exit" + System.lineSeparator ();
		try {
			executeScript (script);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace ();
		}
	}

	public void stop () {
		signal ("stop");
	}

	public void reload () {
		signal ("reload");
	}

	private void signal (String signal) {
		int pos = path.getAbsolutePath ().indexOf (":");
		String script = path.getAbsolutePath ().substring (0, pos) + ":" + System.lineSeparator ();
		script += "CD \"" + path.getAbsolutePath () + "\"" + System.lineSeparator ();
		script += "start nginx.exe -s " + signal + System.lineSeparator ();
		// script += "start nginx.exe" + System.lineSeparator ();
		script += "exit" + System.lineSeparator ();
		try {
			executeScript (script);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace ();
		}
	}

	private void executeScript (String script) throws IOException {
		File batchfile = File.createTempFile ("nginx.", ".bat");
		OutputStream os = new FileOutputStream (batchfile);
		os.write (script.getBytes ());
		os.close ();

		String command = "cmd /c start cmd.exe /K \"" + batchfile.getAbsolutePath () + "\"";
		Runtime.getRuntime ().exec (command);

		batchfile.deleteOnExit ();
	}

	static public String resourceToString (URI resource) throws IOException {
		byte[] bytes = Files.readAllBytes (Paths.get (resource));
		return Charset.defaultCharset ().decode (ByteBuffer.wrap (bytes)).toString ();
	}

	@Deprecated
	static public URI findResource (String resource) throws ResourceNotFound, IOException {
		File potential;
		// 1. check the executable source
		CodeSource src = NGinX.class.getProtectionDomain ().getCodeSource ();
		if ((src != null) && (src.getLocation () != null)) {
			potential = new File (src.getLocation ().getFile (), resource);
			if (potential.exists ()) {
				return potential.toURI ();
			}
		}

		// 2. check in classpath directories
		for (String cpitem:System.getProperty ("java.class.path").split (";")) {
			File root = new File (cpitem);
			if (root.isDirectory ()) {
				potential = new File (root, resource);
				if (potential.exists ())
					return potential.toURI ();
			}
		}

		// 3. check in classpath libraries (JAR files)
		for (String cpitem:System.getProperty ("java.class.path").split (";")) {
			File root = new File (cpitem);
			if (root.isFile ()) {
				ZipInputStream zip = null;
				try {
					zip = new ZipInputStream (new FileInputStream (root));
					while (true) {
						ZipEntry e = zip.getNextEntry ();
						if (e == null)
							break;
						if (e.getName ().toLowerCase ().indexOf (".dll") > 0) {
							String name = new File (e.getName ()).getName ();
							long size = e.getSize ();
							File fsfile = new File (name);
							if (!fsfile.exists ()) {
								Trace.info (name + " not found, need to copy");
								// extractFile (zip, e, fsfile);
							} else {
								Trace.info (name + ": size " + size + "/" + fsfile.length () + ": date " + e.getTime () + "/" + fsfile.lastModified ());
								// if (size != fsfile.length ()) {
								// s += " -- side doesn't match, need to copy";
								// extractFile (zip, e, fsfile);
								// }
							}

						}
					}
				} catch (FileNotFoundException e) {
					throw new IShellException.ResourceNotFound (resource);
				} finally {
					if (zip != null) {
						zip.close ();
					}
				}
			}
		}
		throw new IShellException.ResourceNotFound (resource);
	}

}

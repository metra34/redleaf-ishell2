/*
 * iShell 2.0
 *
 * Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
 *
 * This library is proprietary software; you can not redistribute
 * without an explicit consent from Releaf Solutions Ltd.
 * The consent will detail the distribution and sale rights.
 */

package ca.redleafsolutions.ishell2.interfaces.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellInputStream;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellObject.ExecutedObject;
import ca.redleafsolutions.ishell2.IShellRedirectable;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.IShellRequestHTTP;
import ca.redleafsolutions.ishell2.IShellResponse;
import ca.redleafsolutions.ishell2.ParseRequestResults;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.interfaces.IShellHTTPInterface;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class InternalHTTPServer extends IShellHTTPInterface {
	//	private JSONValidator validator;
	private Map<String, HttpHandler> virtualhosts;

	public InternalHTTPServer (iShell main, JSONItem params)
			throws IOException, JSONValidationException, SingletonException {
		super (main, params);

		virtualhosts = new TreeMap<String, HttpHandler> ();
		//
		//		validator = new JSONValidator ();
		//		validator.put ("port", new IntegerValidator ().optional (true));
		//		validator.put ("default", new StringValidator ().optional (true));
		//		validator.validate (params);

		String defaultfile = "index.html";
		try {
			defaultfile = params.getString ("default");
		} catch (JSONValidationException e) {
		}

		HttpServer server = HttpServer.create (new InetSocketAddress (port), 0);
		iLogger.info ("iShell HTTP interface opened on port " + port + " with default output format as "
				+ super.defaultOutput);
		//
		// roots = new LinkedHashMap<String, File> ();
		// templates = new LinkedHashMap<String, File> ();
		// relay = new LinkedHashMap<String, URI> ();

		try {
			JSONItem routing = params.getJSON ("routing");
			for (Object okey:routing.listKeys ()) {
				String key = okey.toString ();
				key = key.toLowerCase ();
				JSONItem o = routing.getJSON (key);
				String type = o.getString ("type");
				if (mapping != null)
					mapping.add (key);
				if ("native".equals (type)) {
					String directory = o.getString ("directory");
					server.createContext ("/" + key,
							new InternalHTTPServer.FileHandler (this, key, new File (directory), defaultfile));
				} else if ("template".equals (type)) {
					String directory = o.getString ("directory");
					server.createContext ("/" + key,
							new InternalHTTPServer.TemplateHandler (this, key, new File (directory), defaultfile));
				} else if ("relay".equals (type)) {
					String remote = o.getString ("remote");
					server.createContext ("/" + key, new InternalHTTPServer.RelayHandler (this, key, URI.create (remote)));
				}
			}
		} catch (JSONValidationException e) {
		}

		try {
			JSONItem vhosts = params.getJSON ("virtualhost");
			for (Object okey:vhosts.listKeys ()) {
				String key = okey.toString ();
				key = key.toLowerCase ();
				JSONItem o = vhosts.getJSON (key);
				String type = o.getString ("type");
				if (mapping != null)
					mapping.add (key);

				if ("native".equals (type)) {
					String directory = o.getString ("directory");
					virtualhosts.put (key,
							new InternalHTTPServer.FileHandler (this, key, new File (directory), defaultfile));
				} else if ("template".equals (type)) {
					String directory = o.getString ("directory");
					virtualhosts.put (key,
							new InternalHTTPServer.TemplateHandler (this, key, new File (directory), defaultfile));
				} else if ("relay".equals (type)) {
					String remote = o.getString ("remote");
					virtualhosts.put (key, new InternalHTTPServer.RelayHandler (this, key, URI.create (remote)));
				}
			}
		} catch (JSONValidationException e) {
		}

		server.createContext ("/", new InternalHTTPServer.RootHandler (this));
		server.setExecutor (java.util.concurrent.Executors.newCachedThreadPool ());
		server.start ();
	}

	static abstract class BaseHandler implements HttpHandler {
		protected InternalHTTPServer iface;
		protected String key;

		public BaseHandler (InternalHTTPServer iface, String key) {
			this.iface = iface;
			this.key = key;
		}

		abstract protected byte[] _handle (IShellRequest request) throws IShellException.ResourceNotFound, IOException;

		public void handle (HttpExchange t) throws IOException {
			OutputStream os = t.getResponseBody ();
			try {
				URI uri = t.getRequestURI ();
				IShellRequestHTTP request = new IShellRequestHTTP (new ParseRequestResults (uri, key));
				request.setRequestHeaders (new ObjectMap (t.getRequestHeaders ()));
				int reqid = iLogger.logIShellRequest (request, t.getRemoteAddress ().getAddress ());

				try {
					// iLogger.info (t.getRemoteAddress ().getAddress () + ">> "
					// + uri.toString ());
					byte[] bytes = _handle (request);
					t.sendResponseHeaders (200, bytes.length);
					os.write (bytes);
					iLogger.logIShellResponse (reqid, " HTTP (200) length: " + bytes.length);
				} catch (IShellException.ResourceNotFound e) {
					String s = "'" + e.getResource () + "' was not found (404)";
					t.sendResponseHeaders (404, s.length ());
					os.write (s.getBytes ());
					iLogger.logIShellResponse (reqid, " HTTP (404) - ", e.toString ());
				} catch (IOException e) {
					String s = "Internal server error (500)";
					t.sendResponseHeaders (500, s.length ());
					os.write (s.getBytes ());
					iLogger.logIShellResponse (reqid, " HTTP (500) - ", e.toString ());
				}
			} catch (Throwable e) {
				iLogger.severe (e.toString ());
				e.printStackTrace ();
			} finally {
				t.getResponseBody ().close ();
			}
		}
	}

	static class FileHandler implements HttpHandler {
		protected InternalHTTPServer iface;
		protected String key;
		protected File root;
		protected String defaultfile;

		public FileHandler (InternalHTTPServer iface, String key, File file, String defaultfile) {
			this.iface = iface;
			this.key = key;
			this.root = file;
			this.defaultfile = defaultfile;
		}

		@Override
		/** to allow streaming back to back from file directly to output stream,
		 * need to override this method */
		public void handle (HttpExchange t) throws IOException {
			long tic = System.nanoTime ();
			InputStream is = null;
			OutputStream os = t.getResponseBody ();
			try {
				URI uri = t.getRequestURI ();
				ParseRequestResults parsed = new ParseRequestResults (uri, key);
				IShellRequestHTTP request = new IShellRequestHTTP (parsed);
				request.setRequestHeaders (new ObjectMap (t.getRequestHeaders ()));
				int reqid = iLogger.logIShellRequest (request, t.getRemoteAddress ().getAddress ());

				try {
					File file = new File (root, request.getPathString ());
					// if file is a directory, append the default file to it
					if (file.isDirectory ()) {
						file = new File (file, defaultfile);
					}

					if (!file.exists ()) {
						String s = "'" + file.getCanonicalPath () + "' was not found (404)";
						t.sendResponseHeaders (404, s.length ());
						os.write (s.getBytes ());
						iLogger.logIShellResponse (reqid, " HTTP (404) - ", file.getCanonicalPath ());
						return;
					}

					// make sure to set output to the proper format
					String frmt = uri.getPath ();
					int pos = frmt.lastIndexOf ('.');
					if (pos > 0) {
						frmt = frmt.substring (pos);
					}
					String txmime = IShellHTTPInterface.getMIMEType (frmt);
					if (txmime != null) {
						Headers map = t.getResponseHeaders ();
						map.set ("Content-Type", txmime);
						if (!txmime.startsWith ("text/")) {
							map.set ("Accept-Ranges", "bytes");
						}
						map.set ("Server", "iShell/HTTP");
					}

					StreamAndLength strmlen;
					if ((txmime != null) && !txmime.startsWith ("text/")) {
						strmlen = new StreamAndLength (file, request);
					} else {
						strmlen = getStreamAndLength (file, request);
					}

					t.sendResponseHeaders (200, strmlen.length ());

					is = strmlen.getInputStream ();
					int len;
					byte[] buff = new byte[1024];
					while ((len = is.read (buff)) > 0) {
						os.write (buff, 0, len);
					}

					long duration = (System.nanoTime () - tic) / 1000000;
					iLogger.logIShellResponse (reqid, " HTTP (200) length: " + file.length (), " bytes. Duration: ",
							duration + "ms");
				} catch (IOException e) {
					String s = "Internal server error (500)";
					t.sendResponseHeaders (500, s.length ());
					os.write (s.getBytes ());
					iLogger.logIShellResponse (reqid, " HTTP (500) - ", e.toString ());
				}
			} catch (Throwable e) {
				iLogger.severe (e.toString ());
				e.printStackTrace ();
			} finally {
				if (is != null)
					is.close ();
				t.getResponseBody ().close ();
			}
		}

		protected StreamAndLength getStreamAndLength (File file, IShellRequest request) throws IOException {
			return new StreamAndLength (file, request);
		}
	}

	static class TemplateHandler extends FileHandler {
		public TemplateHandler (InternalHTTPServer iface, String key, File dir, String defaultFile) {
			super (iface, key, dir, defaultFile);
		}

		@Override
		protected StreamAndLength getStreamAndLength (File file, IShellRequest request) throws IOException {
			ObjectMap map = IShellHTTPInterface.templateSetup (file.toString (), request);

			File tmpfile = File.createTempFile (file.getName (), "tmp");
			Writer writer = new FileWriter (tmpfile);
			InputStream is = null;
			try {
				is = new FileInputStream (file);
				writer.append (TemplateUtils.evaluate (is, map));
			} catch (FileNotFoundException e) {
				try {
					is = new FileInputStream (new File (root, defaultfile));
					writer.append (TemplateUtils.evaluate (is, map));
				} catch (FileNotFoundException e1) {
					iLogger.severe ("No default file found " + e1);
				}
			} finally {
				if (is != null) {
					try {
						is.close ();
					} catch (IOException e) {
						iLogger.severe (e);
					}
				}
				if (writer != null) {
					try {
						writer.close ();
					} catch (IOException e) {
						iLogger.severe (e);
					}
				}
			}
			return new StreamAndLength (tmpfile, request);
		}

		// @Override
		// protected byte[] _handle (IShellRequest request) {
		// // if resource not available: 404
		// // return false;
		// VelocityContext map = new VelocityContext ();
		// for (Entry<String, Object> entry: iface.main.extensions ().entrySet
		// ()) {
		// map.put (entry.getKey (), entry.getValue ());
		// }
		//
		// Reader reader = new FileReader (file);
		// StringWriter sw = new StringWriter ();
		// try {
		// Velocity.evaluate (map, sw, request.getPathString () + " handler",
		// reader);
		// } catch (ResourceNotFoundException e) {
		// sw.write ("<h1>404 - Not Found</h1>");
		// } catch (ParseErrorException e) {
		// sw.write ("<h1>500 - Parse Error</h1>");
		// sw.write (e.toString ());
		// } catch (MethodInvocationException e) {
		// sw.write ("<h1>500 - Server Error</h1>");
		// sw.write (e.toString ());
		// } finally {
		//
		// }
		// return sw.toString ().getBytes ();
		// }
	}

	static class RelayHandler extends BaseHandler implements HttpHandler {
		private URI uri;

		public RelayHandler (InternalHTTPServer iface, String key, URI uri) {
			super (iface, key);
			this.uri = uri;
		}

		@Override
		public void handle (HttpExchange t) throws IOException {
			try {
				// parse the URL
				URI _uri = t.getRequestURI ();
				Headers hhh = t.getRequestHeaders ();
				ParseRequestResults parsed = new ParseRequestResults (_uri, key);

				String u = uri.toString ();
				if (parsed.getParams ().size () > 0) {
					u += "?";
				}
				for (Entry<String, Object> entry:parsed.getParams ().entrySet ()) {
					u += entry.getKey () + "=" + entry.getValue ();
				}

				try {
					URL url = new URL (u);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection ();
					conn.setRequestMethod ("GET");
					for (Entry<String, List<String>> entry:hhh.entrySet ()) {
						String key = entry.getKey ();
						if (key != null) {
							for (String value:entry.getValue ()) {
								if ("host".equalsIgnoreCase (key)) {
									value = uri.getHost ();
								}
								conn.setRequestProperty (key, value);
							}
						}
					}

					for (Entry<String, List<String>> entry:conn.getHeaderFields ().entrySet ()) {
						String key = entry.getKey ();
						if (key != null) {
							for (String value:entry.getValue ()) {
								t.getResponseHeaders ().add (key, value);
							}
						}
					}

					long l = conn.getContentLengthLong ();
					t.sendResponseHeaders (200, Math.max (0, l));

					byte[] buff = new byte[4096];
					int len;
					while ((len = conn.getInputStream ().read (buff)) > 0) {
						// String ddd = new String (buff, 0, len);
						t.getResponseBody ().write (buff, 0, len);
					}
				} catch (Exception e) {
					e.printStackTrace ();
				}
			} catch (Throwable e) {
				e.printStackTrace ();
			} finally {
				t.getResponseBody ().close ();
			}
		}

		@Override
		protected byte[] _handle (IShellRequest request) {
			throw new RuntimeException ("Code should of never reach this point");
		}
	}

	static public class RootHandler implements HttpHandler {
		private InternalHTTPServer iface;

		public RootHandler (InternalHTTPServer iface) {
			this.iface = iface;
		}

		@Override
		public void handle (HttpExchange t) throws IOException {
			ParseRequestResults parsed = null;
			try {
				// if call to a virtual host then trasfer the handling of
				// request
				URI uri = t.getRequestURI ();
				if (iface.virtualhosts.size () > 0) {
					String host = t.getRequestHeaders ().getFirst ("Host");
					if (iface.virtualhosts.containsKey (host)) {
						HttpHandler otherhandler = iface.virtualhosts.get (host);
						otherhandler.handle (t);
						return;
					}
				}

				// parse request URI
				parsed = new ParseRequestResults (uri);

				// read the header
				MimeHandler mime = new MimeHandler ();
				Headers headers = t.getRequestHeaders ();
				if (headers.size () > 0) {
					for (String key:headers.keySet ()) {
						for (String value:headers.get (key)) {
							if (mime.addHeaderEntry (key, value))
								break;
						}
					}
				}

				// parse request for POST parameters of Multipart data
				mime.parseRequest (t.getRequestBody ());
				parsed.addParams (mime.getParams ());

				if (!parsed.hasFormatExtension () && parsed.getParams ().size () <= 0) {
					// otherwise: conform to / treminated format
					String path = uri.getPath ();
					int li = path.lastIndexOf ("/");
					if (li != path.length () - 1) {
						String uris = uri.getPath () + "/";
						if (uri.getQuery () != null)
							uris += "?" + uri.getQuery ();
						URI uri2 = URI.create (uris);
						Headers map = t.getResponseHeaders ();
						map.set ("Location", uri2.toString ());
						t.sendResponseHeaders (301, -1);
						return;
					}
				}

				Headers map = t.getResponseHeaders ();
				// make sure to set output to the proper format
				String frmt = parsed.getOutputFormat (iface.defaultoutput ());
				String txmime = IShellHTTPInterface.getMIMEType (frmt);
				if (txmime != null) {
					map.set ("Content-Type", txmime);
				}

				for (String key:iface.getHeaders ().keySet ()) {
					map.set (key, iface.getHeaders ().get (key));
				}

				// execute the request
				OutputStream os = t.getResponseBody ();
				int status = 200;
				IShellRequestHTTP request = new IShellRequestHTTP (parsed);
				request.setRequestHeaders (new ObjectMap (t.getRequestHeaders ()));
				int reqid = iLogger.logIShellRequest (request, t.getRemoteAddress ().getAddress ());

				IShellResponse response = iface.executeAndRespond (request, parsed);

				// if an exception: handle it gracefully
				if (response.getResponse () instanceof IShellObject.ExceptionObject) {
					status = 404;
				}

				// handle the special cases of InputStream or Redirection
				// request returned
				boolean responseSent = false;
				if (response.getResponse () instanceof IShellObject.ExecutedObject) {
					ExecutedObject respobj = (IShellObject.ExecutedObject)(response.getResponse ());
					if (respobj.getObject () instanceof IShellInputStream) {
						IShellInputStream is = (IShellInputStream)respobj.getObject ();
						if (is.getMimeType () != null) {
							map.set ("Content-Type", is.getMimeType ());
						}
						t.sendResponseHeaders (status, is.length ());
						is.tunnelTo (os);
						is.close ();
						responseSent = true;
						iLogger.logIShellResponse (reqid, ": stream tunneled through. Length " + is.length ());
					} else if (respobj.getObject () instanceof IShellDownloadable) {
						IShellDownloadable ds = (IShellDownloadable)respobj.getObject ();
						map.set ("Content-Type", ds.getMimeType ());
						map.set ("Content-Disposition", "attachment;filename=" + ds.getFilename ());
						t.sendResponseHeaders (status, ds.length ());
						os.write (ds.getBuffer ());
						responseSent = true;
						iLogger.logIShellResponse (reqid,
								": Downloading " + ds.length () + " bytes as " + ds.getFilename ());
						ds.close ();
					} else if (respobj.getObject () instanceof IShellRedirectable) {
						IShellRedirectable redirect = (IShellRedirectable)respobj.getObject ();
						map.add ("Location", redirect.getUrl ().toString ());
						status = 307;
					}
				}

				if (response.getResponse () instanceof IShellObject.RawObject) {
					IShellObject.RawObject respobj = (IShellObject.RawObject)(response.getResponse ());
					if (respobj.getObject () instanceof IShellRedirectable) {
						IShellRedirectable redirect = (IShellRedirectable)respobj.getObject ();
						map.add ("Location", redirect.getUrl ().toString ());
						status = 307;
					}
				}

				if (!responseSent) {
					// handle the general case
					String s = response.toString (); // iface.generateOutput
					// (parsed.getOutputFormat
					// (), response);
					byte[] bytes = (s != null)? s.getBytes (): "".getBytes ();
					t.sendResponseHeaders (status, bytes.length);
					os.write (bytes);
					iLogger.logIShellResponse (reqid, ": ", s);
				}
			} catch (Throwable e) {
				iLogger.severe (e);
			} finally {
				t.getResponseBody ().close ();
				t.close ();
				// attempt to delete any uploaded files (temporary)
				if (parsed != null) {
					for (Object o:parsed.getParams ().values ()) {
						if (o instanceof File) {
							((File)o).delete ();
						}
					}
				}
			}
		}
	}

	@Override
	public String info () {
		return "HTTP server on port " + port;
	}
}

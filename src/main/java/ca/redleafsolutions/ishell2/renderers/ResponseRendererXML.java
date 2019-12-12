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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellObject.ExecutedObject;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.XMLWritable;
import ca.redleafsolutions.json.JSONItem;

public class ResponseRendererXML extends ResponseRendererBase {
	public ResponseRendererXML (double timing) {
		super (timing);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public String toString (Object o, IShellRequest request) {
		if (o instanceof Throwable) {
			return toExceptionString ((Throwable)o, request);
		} else {
			if (o instanceof ExecutedObject) {
				ExecutedObject exobj = (ExecutedObject)o;
				o = exobj.getObject ();
			}
		}

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance ();
		DocumentBuilder docBuilder;
		// if result is a String try to parse into XML
		if (o instanceof String) {
			try {
				docBuilder = docFactory.newDocumentBuilder ();
				InputStream s = new ByteArrayInputStream (((String)o).getBytes ("UTF-8"));
				Document document = docBuilder.parse (s);
				return XML2String (document);
			} catch (ParserConfigurationException e) {
				e.printStackTrace ();
			} catch (SAXException e) {
				e.printStackTrace ();
			} catch (IOException e) {
				e.printStackTrace ();
			} catch (TransformerException e) {
				e.printStackTrace ();
			}
		}

		// if parse failed, build and XML output
		try {
			docFactory = DocumentBuilderFactory.newInstance ();
			docBuilder = docFactory.newDocumentBuilder ();
			Document doc = docBuilder.newDocument ();

			if (o instanceof XMLWritable) {
				((XMLWritable)o).toXML (doc);
			} else {
				Element rootElement = doc.createElement ("response");
				doc.appendChild (rootElement);
				rootElement.setAttribute ("status", "ok");
				if (o == null) {
				} else if (o instanceof String) {
					rootElement.setTextContent (o.toString ());
				} else if (o instanceof Character) {
					rootElement.setTextContent (o.toString ());
				} else if (o instanceof Number) {
					int i = ((Number)o).intValue ();
					double d = ((Number)o).doubleValue ();
					if (i == d) {
						rootElement.setTextContent (Integer.toString (i));
					} else {
						rootElement.setTextContent (Double.toString (d));
					}
				} else if (o instanceof Boolean) {
					rootElement.setTextContent ((Boolean.toString ((Boolean)o)));
				} else if (o instanceof Map) {
					for (Entry<? extends Object, ? extends Object> entry: ((Map<? extends Object, ? extends Object>)o)
							.entrySet ()) {
						Element mapElement = doc.createElement (entry.getKey ().toString ());
						doc.appendChild (rootElement);
						mapElement.setTextContent (entry.getValue ().toString ());
					}
				} else if (o instanceof Iterable) {
					JSONItem jsonarr = JSONItem.newArray ();
					for (Object item: (Iterable<? extends Object>)o) {
						Element mapElement = doc.createElement ("item");
						doc.appendChild (rootElement);
						mapElement.setTextContent (item.toString ());
					}
					return jsonarr.toString (3);
				} else {
					// rootElement.appendChild (toXMLObject (o));
					rootElement.setTextContent (o.toString ());
				}
			}

			return XML2String (doc);
		} catch (Throwable e) {
			return toExceptionString (e, request);
		}
	}

	private String XML2String (Document doc) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance ();
		Transformer transformer = transformerFactory.newTransformer ();
		StringWriter writer = new StringWriter ();
		transformer.transform (new DOMSource (doc), new StreamResult (writer));
		String output = writer.getBuffer ().toString ().replaceAll ("\n|\r", "");
		return output;
	}

	@Override
	public String toExceptionString (Throwable throwable, IShellRequest request) {
		if (throwable instanceof IShellException.InvocationTargetException) {
			return toExceptionString (((IShellException.InvocationTargetException)throwable).getException (), request);
		}

		String output;

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance ();
		DocumentBuilder docBuilder;
		StackTraceElement[] trace = throwable.getStackTrace ();
		try {
			docBuilder = docFactory.newDocumentBuilder ();
			Document doc = docBuilder.newDocument ();

			Element rootElement = doc.createElement ("response");
			doc.appendChild (rootElement);
			rootElement.setAttribute ("status", "error");
			rootElement.setAttribute ("type", throwable.getClass ().getSimpleName ());
			rootElement.setAttribute ("message", throwable.getMessage ());

			Element traceList = doc.createElement ("stacktrace");
			rootElement.appendChild (traceList);
			for (StackTraceElement tr: trace) {
				Element traceElement = doc.createElement ("trace");
				traceList.appendChild (traceElement);
				traceElement.setAttribute ("file", tr.getFileName ());
				traceElement.setAttribute ("line", "" + tr.getLineNumber ());
				traceElement.setAttribute ("class", tr.getClassName ());
				traceElement.setAttribute ("method", tr.getMethodName ());
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance ();
			Transformer transformer = transformerFactory.newTransformer ();
			StringWriter writer = new StringWriter ();
			transformer.transform (new DOMSource (doc), new StreamResult (writer));
			output = writer.getBuffer ().toString ().replaceAll ("\n|\r", "");
		} catch (Throwable e) {
			output = "<response";
			output += " \"status\"=\"error\"";
			output += " \"type\"=\"" + throwable.getClass ().getSimpleName () + "\"";
			output += " \"message\"=\"" + throwable.getMessage () + "\"";
			output += ">";

			output += "<stacktrace>";
			for (StackTraceElement tr: trace) {
				output += "<trace";
				output += " \"file\"=\"" + tr.getFileName () + "\"";
				output += " \"line\"=\"" + tr.getLineNumber () + "\"";
				output += " \"class\"=\"" + tr.getClassName () + "\"";
				output += " \"method\"=\"" + tr.getMethodName () + "\"";
				output += " />";
			}
			output += "</stacktrace>";
			output = "</response>";
		}

		return output;
	}

	@Override
	public String toDetails (Object o, IShellRequest request) {
		throw new RuntimeException ("Not implemented yet");
	}
}

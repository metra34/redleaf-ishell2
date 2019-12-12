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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ca.redleafsolutions.Instantiator;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.ui.IShellUI;
import ca.redleafsolutions.ishell2.ui.JSUI;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public abstract class IShellHTTPInterface extends IShellInterfaceImpl {
	protected String serverurl;
	protected int port;
	protected Map<String, String> headers;
	private IShellUI ui;

	public IShellHTTPInterface (iShell main, JSONItem params) throws JSONValidationException, IOException, SingletonException {
		super (main, params);
		
		String sport;
		try {
			port = params.getInt ("port");
		} catch (JSONValidationException e) {
			try {
				sport = params.getString ("port");
				port = Integer.parseInt (sport);
			} catch (JSONValidationException e1) {
				port = 80;
			}
		}

		try {
			serverurl = params.getString ("serverurl");
		} catch (JSONValidationException e) {
			serverurl = "http://localhost" + ((port != 80)? ":" + port: "") + "/";
		}

		try {
			JSONItem uijson = params.getJSON ("ui");

			String uiclass;
			try {
				uiclass = uijson.getString ("class");
			} catch (JSONValidationException e) {
				uiclass = JSUI.class.getName ();
			}
			
			try {
				ui = new Instantiator<IShellUI> ().instantiate (uiclass, uijson.getJSON ("params"));
			} catch (InstantiationException e) {
				ui = new JSUI (uijson.getJSON ("params"));
			}
			
			String extension;
			try {
				extension = uijson.getString ("class");
			} catch (JSONValidationException e) {
				extension = "ui";
			}
			main.engine ().extend (extension, ui);
		} catch (JSONValidationException e) {
		}
		
		headers = new HashMap<String, String> ();
		try {
			JSONItem reqheaders = params.getJSON ("response-headers");
			for (Object okey:reqheaders.listKeys ()) {
				String key = okey.toString ();
				headers.put (key, reqheaders.getString (key));
			}
		} catch (JSONValidationException e) {
		}
	}

	@Override
	protected String getNativeFormat () {
		return "HTML";
	}

	public String getServerURL () {
		return serverurl;
	}
	

	public void addHeader (String key, String value) {
		this.headers.put (key, value);
	}
	
	public Map<String, String> getHeaders () {
		return this.headers;
	}

	public static String getMIMEType (String extension) {
		if (extension.indexOf ('.') == 0)
			extension = extension.substring (1);
		extension = extension.toLowerCase ();
		
		if ("css".equals (extension))
			return "text/css";
		if ("html".equals (extension) || "htm".equals (extension) || "vm".equals (extension))
			return "text/html";
		if ("txt".equals (extension) || "text".equals (extension))
			return "text/plain";
		if ("json".equals (extension))
			return "application/json";
		if ("js".equals (extension))
			return "application/javascript";
		if ("png".equals (extension))
			return "image/png";
		if ("jpg".equals (extension) || "jpeg".equals (extension))
			return "image/jpeg";
		if ("xml".equals (extension))
			return "application/xml";
		if ("md".equals (extension))
			return "text/markdown";
		return null;
	}
	
	static public ObjectMap templateSetup (Object own) {
		return templateSetup (own, null);
	}
	static public ObjectMap templateSetup (Object own, IShellRequest request) {
		ObjectMap map = new ObjectMap ();
		map.putAll (iShell.getInstance ().extensions ());
		map.put ("this", own);
		map.put ("ui", iShell.getInstance ().http ().getUI ());
		if (request != null)
			map.put ("request", request);
		return map;
	}
	
	@Override
	public boolean isBrowserInterface () {
		return true;
	}

	public IShellUI getUI () {
		return ui;
	}
}

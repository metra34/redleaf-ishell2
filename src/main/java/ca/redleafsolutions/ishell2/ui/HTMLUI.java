package ca.redleafsolutions.ishell2.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.HTMLWritable;
import ca.redleafsolutions.ishell2.IShellRequestConsumer;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.iShell;

public class HTMLUI implements HTMLWritable, IShellRequestConsumer {
	private Class<? extends Object> cls;
	private String resourcename;
	private ObjectMap map;

	public HTMLUI (Class<? extends Object> cls, String resourcename, ObjectMap map) {
		this.cls = cls;
		this.resourcename = resourcename;
		this.map = map;
		map.put ("ui", iShell.getInstance ().http ().getUI ());
	}

	public String build () throws FileNotFoundException {
		ResourceLocator locator = new ResourceLocator (cls);
		InputStream is = locator.getInputStream (resourcename);
		if (is == null) {
			throw new FileNotFoundException (resourcename);
		}
		try {
			return TemplateUtils.evaluate (is, map);
		} finally {
			if (is != null)
				try {
					is.close ();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

	@Override
	public String toHTML () {
		try {
			return build ();
		} catch (FileNotFoundException e) {
			return "<html><body>" + e.getClass ().getSimpleName () + ": " + e.getMessage () + "</body></html>";
		}
	}

	@Override
	public void setRequest (IShellRequestSingle request) {
		map.put ("request", request);
	}
}

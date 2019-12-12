package ca.redleafsolutions.ishell2.logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import ca.redleafsolutions.ObjectList;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellInputStream;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class FileViewer {
	private ObjectList roots;
	private File cwd;

	public FileViewer (JSONItem json) throws JSONValidationException {
		roots = new ObjectList ();
		try {
			JSONItem rootj = json.getJSON ("root");
			if (rootj.isObject ())
				throw new JSONValidationException.IllegalValue ("root", rootj.toString ());
			for (int i = 0; i < rootj.length (); ++i) {
				try {
					roots.add (new File (rootj.getString (i)));
				} catch (JSONValidationException e) {
					throw e;
				}
			}
		} catch (JSONValidationException.IllegalValue e) {
			try {
				String root = json.getString ("root");
				roots.add (new File (root));
			} catch (JSONValidationException.IllegalValue e1) {
				throw new JSONValidationException.IllegalValue ("root", json.get ("root"));
			}
		}
	}

	@MethodDescription ("Change current working directory")
	@ParameterNames ("path")
	@ParameterDescriptions ("Path")
	public void cd (String path) {
		for (String pathelement:path.split ("/")) {
			if (!"..".equals (pathelement)) {
				if (cwd != null) {
					cwd = new File (cwd, path);
				} else {
					cwd = new File (path);
				}
			} else {
				if (cwd != null) {
					cwd = cwd.getParentFile ();
				}
			}
		}
	}

	@MethodDescription ("Get current working directory")
	public File pwd () {
		return cwd;
	}

	public File abspwd () {
		return cwd.getAbsoluteFile ();
	}

	@MethodDescription ("Get information about a file")
	@ParameterNames ("fname")
	@ParameterDescriptions ("File name")
	public ObjectMap info (String fname) throws FileNotFoundException {
		ObjectMap map = new ObjectMap ();
		File file = null;
		if (cwd == null) {
			for (Object rooto:roots) {
				File root = (File)rooto;
				if (fname.equalsIgnoreCase (root.getName ()))
					file = root;
			}
		} else {
			file = new File (cwd, fname);
		}
		
		if ((file == null) || (!file.exists ())) {
			throw new FileNotFoundException (fname);
		}
		
		map.put ("name", file.getName ());
		map.put ("type", file.isFile ()? "file": file.isDirectory ()? "dir": "UNKNOWN");
		if (file.isFile ())
			map.put ("size", file.length ());

		ObjectMap modified = new ObjectMap ();
		map.put ("modified", modified);
		modified.put ("ts", file.lastModified ());
		modified.put ("format", new Date (file.lastModified ()).toString ());

		ObjectList attr = new ObjectList ();
		if (file.isHidden ())
			attr.add ("hidden");
		if (file.isAbsolute ())
			attr.add ("absolute");
		if (file.canExecute ())
			attr.add ("execute");
		if (file.canRead ())
			attr.add ("read");
		if (file.canWrite ())
			attr.add ("write");
		if (attr.size () > 0)
			map.put ("attributes", attr);
		return map;
	}

	@MethodDescription ("List files in current working directory")
	public ObjectList list () {
		if (cwd == null)
			return roots;

		ObjectList list = new ObjectList ();
		for (File file:cwd.listFiles ()) {
			list.add (file.getName ());
		}
		return list;
	}

	@MethodDescription ("Read an entire file")
	@ParameterNames ("fname")
	@ParameterDescriptions ("File name")
	public IShellInputStream read (String fname) throws FileNotFoundException {
		File file = cwd != null? new File (cwd, fname): new File (fname);
		return new IShellInputStream (new FileInputStream (file), file.length ());
	}

	@MethodDescription ("Filter files using an HTML template")
	public String filter () throws IOException {
		ResourceLocator locator = new ResourceLocator (iLogger.class);
		InputStream is = locator.getInputStream ("fileviewer.html");
		if (is == null) {
			throw new FileNotFoundException ("fileviewer.html");
		}
		ObjectMap map = new ObjectMap ();
		map.put ("roots", roots);
		map.put ("cwd", cwd);
		map.put ("path", cwd != null? getPathElements (): null);
		map.put ("files", list ());
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

	private ObjectList getPathElements () {
		ObjectList path = new ObjectList ();
		File tmp = cwd;
		while (tmp != null) {
			path.push (tmp.getName ());
			tmp = tmp.getParentFile ();
		}
		return path;
	}
}

package ca.redleafsolutions.ishell2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.TemplateUtils;

public class iShellTemplate implements HTMLWritable {
	private static File root = null;

	public static void setRoot (String dirname) throws IOException {
		root = new File (dirname);
		if (!root.exists ())
			root.mkdirs ();
		if (root.isFile ())
			throw new IOException (root.getAbsolutePath () + " is a file. Can't use as template root.");
	}

	private String filename;
	private ObjectMap map;

	public iShellTemplate (String filename, ObjectMap map) {
		this.filename = filename;
		this.map = map;
	}

	@Override
	public String toString () {
		return toHTML ();
	}

	@Override
	public String toHTML () {
		InputStream is = null;
		try {
			if (root != null) {
				File file = new File (root, filename);
				if (file.exists ())
					is = new FileInputStream (file);
			}
			if (is == null) {
				is = new ResourceLocator (this.getClass ()).getInputStream (filename);
			}
			return TemplateUtils.evaluate (is, map, "RCSTemplate");
		} catch (Exception e) {
			return e.toString ();
		} finally {
			try {
				if (is != null)
					is.close ();
			} catch (IOException e) {
				return e.toString ();
			}
		}
	}
}

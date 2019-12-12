package ca.redleafsolutions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/** Locating resources in the runtime environment: 1) Local file system 2)
 * Classpath file system 3) packed inside JAR files that are in classpath */
public class ResourceLocator {
	private Class<?> cls;

	public ResourceLocator (Class<?> cls) {
		this.cls = cls;
	}

	public InputStream getInputStream (String resource) {
		String path = cls.getPackage ().getName ().replace ('.', '/') + "/" + resource;

//		Trace.info (1, path);
		// check local file system
		File file = new File (resource);
//		Trace.info (2, file.getAbsolutePath ());
		if (!file.exists ())
			file = new File (path);
//		Trace.info (3, file.getAbsolutePath ());
		if (file.exists ())
			try {
				return new FileInputStream (file);
			} catch (FileNotFoundException e) {
				// do nothing
			}

//		Trace.info (4);
		// check class path file system
		String classpath = System.getProperty ("java.class.path");
		for (String pathelement:classpath.split (java.io.File.pathSeparator)) {
			File dir = new File (pathelement);
			if (dir.isDirectory ()) {
				file = new File (dir, path);
//				Trace.info (5, file.getAbsolutePath ());
				if (!file.exists ())
					file = new File (dir, resource);
//				Trace.info (6, file.getAbsolutePath ());
				if (file.exists ())
					try {
						return new FileInputStream (file);
					} catch (FileNotFoundException e) {
						// do nothing
					}
			}
		}

//		Trace.info (7, path);
		// check JARs
		return this.getClass ().getClassLoader ().getResourceAsStream (path);
	}
}

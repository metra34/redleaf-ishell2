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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class Cache_2B_REMOVED {
	private File root;
	private boolean enabled;

	public Cache_2B_REMOVED (String path) {
		root = new File (path);
	}

	public Cache_2B_REMOVED (JSONItem json) throws IOException, JSONValidationException {
		root = new File (json.getString ("root"));
		if (json.getBoolean ("enabled")) {
			on ();
		}
	}

	public void on () throws IOException {
		enabled = false;
		if (!root.exists ()) {
			root.mkdirs ();
		}
		if (!root.exists ())
			throw new IOException ("Error creating cache repository");
		enabled = true;
	}

	public void off () {
		enabled = false;
	}

	public void clear () {
		root.delete ();
	}
	
	public void store (IShellRequestSingle request, IShellObject response) throws IOException {
		if (!enabled)
			return;
		Object o = response.getObject ();
		if (!(o instanceof JSONItem)) {
			return;
		}
		
		File file = new File (root, request.toURL ());
		File dir = file.getParentFile ();
		if (!dir.exists ()) {
			dir.mkdirs ();
		}
		Writer os = new FileWriter (file);
		os.write (o.toString ());
		os.close ();
	}

	public IShellObject.Cached get (IShellRequestSingle request) throws KeyNotFound, IOException {
		if (!enabled)
			return null;
		File file = new File (root, request.toURL ());
		if (!file.exists () || !file.isFile ()) {
			throw new IShellException.KeyNotFound (request.toURL ());
		}
		JSONValidationException json = new JSONValidationException (new String (Files.readAllBytes (Paths.get (request.toString ())), Charset.defaultCharset ()));
		return new IShellObject.Cached (json, request);
	}
}

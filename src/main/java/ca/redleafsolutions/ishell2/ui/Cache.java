package ca.redleafsolutions.ishell2.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class Cache {
	private boolean enabled;
	private File root;

	public Cache (JSONItem json) throws IOException, JSONValidationException {
		try {
			enabled = json.getBoolean ("enabled");
		} catch (JSONValidationException e) {
			enabled = true;
		}
		root = new File (json.getString ("root"));
		if (!root.exists ()) {
			root.mkdirs ();
		} else if (root.isFile ()) {
			throw new IOException ("cache path '" + root + "' is not a directory.");
		}
	}

	public String fetch (String pageid, String resourcename) throws CacheException.NotFound, IOException {
		if (!enabled)
			throw new CacheException.NotFound ();
		File cachefile = new File (new File (root, pageid), resourcename);
		if (!cachefile.exists ()) {
			throw new CacheException.NotFound ();
		}
		return new String (Files.readAllBytes (Paths.get (cachefile.toURI ())));
	}

	public void store (String pageid, String resourcename, String result) throws IOException {
		if (!enabled)
			return;
		File cachefile = new File (new File (root, pageid), resourcename);
		if (!cachefile.getParentFile ().exists ()) {
			cachefile.getParentFile ().mkdirs ();
		}
		Files.write (Paths.get (cachefile.toURI ()), result.getBytes (), StandardOpenOption.CREATE);
	}

	public List<File> list () {
		return recourse (root);
	}

	private List<File> recourse (File root) {
		List<File> list = new LinkedList<> ();
		for (File file:root.listFiles ()) {
			if (file.isDirectory ()) {
				list.addAll (recourse (file));
			} else {
				list.add (file);
			}
		}
		return list;
	}

	public void clear () throws IOException {
		for (File file:recourse (root)) {
			if (!file.delete ())
				throw new IOException ("Failed to delete cache");
		}
	}

	public void enable () {
		enabled = true;
	}

	public void disable () {
		enabled = false;
	}

	public boolean isEnabled () {
		return enabled;
	}
}

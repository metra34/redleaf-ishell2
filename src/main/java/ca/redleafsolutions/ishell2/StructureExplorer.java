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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class StructureExplorer {
	private Object root;

	public StructureExplorer () {
		// TODO Auto-generated constructor stub
	}
	
	public Class<? extends Object> root () {
		return root.getClass ();
	}
	
	public void root (String root) {
		this.root = iShell.getInstance ().extensions ().get (root);
	}
	
	public Map<String, Collection<String>> packages () {
		Map<String, Collection<String>> map = new TreeMap<String, Collection<String>> ();
		for (Package ipkg:Package.getPackages ()) {
			String key = ipkg.getImplementationTitle ();
			if (key == null) key = "";
			if (!map.containsKey (key)) {
				map.put (key, new TreeSet<String> ());
			}
			map.get (key).add (ipkg.getName ());
		}
		return map;
	}
	
	public Collection<String> classes () throws IOException {
		Collection<String> list = new TreeSet<String> ();
		Enumeration<URL> x = ClassLoader.getSystemClassLoader ().getResources ("ca.redleafsolutions.ishell2");
		while (x.hasMoreElements ()) {
			URL url = x.nextElement ();
			list.add (url.toString ());
		}
		return list;
	}
}

package ca.redleafsolutions.ishell2.version;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellException.AlreadyExists;

@Deprecated
public class IShellVersion {
	static private ObjectMap registry = new ObjectMap ();
	
	static public void register (Class<?> cls, String version) throws AlreadyExists {
		register (cls, null, version);
	}

	static public void register (Class<?> cls, String name, String version) throws AlreadyExists {
		if (name == null) {
			name = cls.getName ();
		}
		if (registry.containsKey (name)) throw new IShellException.AlreadyExists ();
		
		String s = version.replace ("$", "");
		s = s.replace ("Id:", "");
		s = s.replace ("Tag:", "");
		s = s.trim ();
		registry.put (name, s);
	}

	static public ObjectMap verions () {
		return registry;
	}
	
	static public ObjectMap ver () {
		ObjectMap map = new ObjectMap ();
		for (String key: registry.keySet ()) {
			String value = registry.get (key).toString ();
			String s = value.replace ("$", "");
			if (s.length () > 9) {
				value = s.substring (s.length () - 9, s.length () - 1);
			}
			map.put (key, value);
		}
		return map;
	}
}

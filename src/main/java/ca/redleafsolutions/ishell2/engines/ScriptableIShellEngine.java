package ca.redleafsolutions.ishell2.engines;

import java.io.File;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;

public interface ScriptableIShellEngine extends IShellEngine {
	File getScriptsRoot ();
	ObjectMap lang ();
	void changeLang (String langname) throws KeyNotFound;
}

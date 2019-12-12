package ca.redleafsolutions.ishell2.engines;

import java.io.File;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ca.redleafsolutions.ObjectList;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequestScript;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class JSR223 extends SimpleEngine implements ScriptableIShellEngine {
	protected ScriptEngine engine;
	private String prompt;
	private File scriptRoot;

	public JSR223 () throws KeyNotFound {
		changeLang ("js");
	}

	public JSR223 (JSONItem json) throws JSONValidationException, KeyNotFound {
		try {
			changeLang (json.getString ("lang"));
		} catch (JSONValidationException.MissingKey e) {
			changeLang ("js");
		}
		try {
			scriptRoot = new File (json.getString ("script-root"));
		} catch (JSONValidationException.MissingKey e) {
			scriptRoot = new File ("scripts");
			if (!scriptRoot.exists ())
				scriptRoot.mkdirs ();
		}
	}

	protected JSR223 (ScriptEngine engine) {
		this.engine = engine;
	}

	@Override
	public IShellObject execute (IShellRequestScript request) {
		try {
			Object o = engine.eval (request.getScript ());
			return new IShellObject.ExecutedObject (o, request);
		} catch (ScriptException e) {
			return new IShellObject.ExceptionObject (e, request);
		}
	}

	@Override
	public void extend (String key, Object ext) {
		super.extend (key, ext);
		if (engine != null)
			engine.put (key, ext);
	}

	@Override
	public void shrink (String key) {
		super.shrink (key);
		if (engine != null) {
			Bindings bindings = engine.getBindings (ScriptContext.ENGINE_SCOPE);
			bindings.remove (key);
		}
	}

	@Override
	public String toString () {
		return engine.get (ScriptEngine.LANGUAGE) + " ver. " + engine.get (ScriptEngine.LANGUAGE_VERSION);
	}

	public ObjectList langs () {
		ObjectList langs = new ObjectList ();
		ScriptEngineManager mgr = new ScriptEngineManager ();
		List<ScriptEngineFactory> factories = mgr.getEngineFactories ();

		for (ScriptEngineFactory factory:factories) {
			langs.add (langFactory2json (factory));
		}
		return langs;
	}

	public ObjectList List () {
		ObjectList langs = new ObjectList ();
		ScriptEngineManager mgr = new ScriptEngineManager ();
		List<ScriptEngineFactory> factories = mgr.getEngineFactories ();

		for (ScriptEngineFactory factory:factories) {
			langs.add (factory.getLanguageName ());
		}
		return langs;
	}

	private ObjectMap langFactory2json (ScriptEngineFactory factory) {
		ObjectMap lang = new ObjectMap ();
		lang.put ("engine-name", factory.getEngineName ());
		lang.put ("engine-version", factory.getEngineVersion ());
		lang.put ("language-name", factory.getLanguageName ());
		lang.put ("language-version", factory.getLanguageVersion ());
		lang.put ("aliases", new ObjectList (factory.getNames ()));
		return lang;
	}

	@Override
	public ObjectMap lang () {
		ObjectMap map = langFactory2json (engine.getFactory ());
		map.put ("global-scope", engine.getBindings (ScriptContext.GLOBAL_SCOPE).keySet ());
		map.put ("engine-scope", engine.getBindings (ScriptContext.ENGINE_SCOPE).keySet ());
		return map;
	}

	@Override
	public void changeLang (String langname) throws KeyNotFound {
		ScriptEngineManager mgr = new ScriptEngineManager ();
		ScriptEngine newengine = mgr.getEngineByName (langname);
		if (newengine == null) {
			throw new IShellException.KeyNotFound (langname);
		}

		Bindings bindings = null;
		if (engine != null) {
			bindings = engine.getBindings (ScriptContext.ENGINE_SCOPE);
		} else {
			bindings = newengine.getBindings (ScriptContext.ENGINE_SCOPE);
			bindings.putAll (extensions);
		}
		newengine.setBindings (bindings, ScriptContext.ENGINE_SCOPE);
		engine = newengine;
		prompt = langname + "> ";
	}

	@Override
	public String getPrompt () {
		return prompt;
	}

	@Override
	public File getScriptsRoot () {
		return scriptRoot;
	}
}

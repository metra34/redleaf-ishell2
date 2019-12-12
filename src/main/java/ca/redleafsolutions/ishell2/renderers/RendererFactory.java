/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.renderers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellException.AlreadyExists;
import ca.redleafsolutions.ishell2.IShellException.KeyNotFound;
import ca.redleafsolutions.ishell2.IShellException.ResourceNotFound;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.ishell2.logs.iLogger;

public class RendererFactory {
	static private RendererFactory instance;

	@IShellInvisible
	static public RendererFactory getInstance () {
		if (instance == null) {
			instance = new RendererFactory ();
		}
		return instance;
	}

	private Set<Class<? extends ResponseRendererBase>> registry;
	private Class<? extends ResponseRendererBase> defaultRenderer;
	private Map<String, Class<? extends ResponseRendererBase>> types;
	private Map<String, Class<? extends ResponseRendererBase>> extentions;

	private RendererFactory () {
		// Singleton instance
		registry = new HashSet<Class<? extends ResponseRendererBase>> ();
		types = new TreeMap<String, Class<? extends ResponseRendererBase>> ();
		extentions = new TreeMap<String, Class<? extends ResponseRendererBase>> ();
	}

	@MethodDescription ("List all registered renderers")
	public Set<Class<? extends ResponseRendererBase>> list () {
		return registry;
	}

	@MethodDescription ("List all supported renderer types")
	public String[] listTypes () {
		String[] strarr = new String[0];
		return types.keySet ().toArray (strarr);
	}

	@MethodDescription ("List all supported extentions")
	public String[] listExtentions () {
		String[] strarr = new String[0];
		return extentions.keySet ().toArray (strarr);
	}

	@MethodDescription ("check if type exists")
	@ParameterNames ("type")
	@ParameterDescriptions ("Type of renderer")
	public boolean typeExists (String type) {
		return types.containsKey (type.toLowerCase ());
	}

	@MethodDescription ("check if extention is associated with a renderer")
	@ParameterNames ("extention")
	@ParameterDescriptions ("Extention name")
	public boolean extentionExists (String ext) {
		return extentions.containsKey (ext.toLowerCase ());
	}

	@IShellInvisible
	public ResponseRendererBase getDefaultRenderer (double timing) throws ResourceNotFound,
			IShellException.InvocationTargetException {
		if (defaultRenderer == null) {
			defaultRenderer = ResponseRendererText.class;
		}
		return _newInstance (defaultRenderer, timing);
	}

	@IShellInvisible
	public void register (String type, String[] extentions, Class<? extends ResponseRendererBase> renderer) throws AlreadyExists {
		if (registry.contains (renderer))
			throw new IShellException.AlreadyExists ();
		registry.add (renderer);
		types.put (type.toLowerCase (), renderer);
		for (String ext: extentions) {
			this.extentions.put (ext.toLowerCase (), renderer);
		}
	}

	@IShellInvisible
	public void unregister (String type) throws KeyNotFound {
		Class<? extends ResponseRendererBase> cls = types.get (type);
		if (cls == null)
			throw new IShellException.KeyNotFound (type);
		
		registry.remove (cls);
		
		types.remove (type);
		
		List<String> toberemoved = new LinkedList<String> ();
		for (Entry<String, Class<? extends ResponseRendererBase>> entry:extentions.entrySet ()) {
			if (entry.getValue ().equals (cls)) {
				toberemoved.add (entry.getKey ());
			}
		}
		for (String key:toberemoved) {
			extentions.remove (key);
		}
	}

	@IShellInvisible
	public void registerDefault (String type, String[] extentions, Class<? extends ResponseRendererBase> renderer)
			throws AlreadyExists {
		if (defaultRenderer != null)
			throw new IShellException.AlreadyExists ();
		for (int i=0; i<extentions.length; ++i)
			extentions[i] = extentions[i].toLowerCase ();
		register (type, extentions, renderer);
		defaultRenderer = renderer;
	}

	@IShellInvisible
	public ResponseRendererBase getRendererByExtention (String extention, double timing)
			throws ResourceNotFound, IShellException.InvocationTargetException {
		Class<? extends ResponseRendererBase> cls = extentions.get (extention.toLowerCase ());
		if (cls == null)
			throw new IShellException.ResourceNotFound (extention);
		return _newInstance (cls, timing);
	}

	@IShellInvisible
	public ResponseRendererBase getRendererByType (String type, double timing)
			throws ResourceNotFound, IShellException.InvocationTargetException {
		Class<? extends ResponseRendererBase> cls = types.get (type.toLowerCase ());
		if (cls == null)
			throw new IShellException.ResourceNotFound (type);
		return _newInstance (cls, timing);
	}

	private ResponseRendererBase _newInstance (Class<? extends ResponseRendererBase> cls, double timing)
			throws ResourceNotFound, IShellException.InvocationTargetException {
		try {
			Constructor<? extends ResponseRendererBase> ctor = cls.getConstructor (double.class);
			ResponseRendererBase renderer = ctor.newInstance (timing);
			return renderer;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			iLogger.severe (e);
			throw new IShellException.InvocationTargetException (e);
		}
	}
}
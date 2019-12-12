/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.engines;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import ca.redleafsolutions.ishell2.IShellException.ExtensionNotFound;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRedirectable;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.IShellRequestScript;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public interface IShellEngine extends IShellRedirectable {
	void extend (JSONItem obj) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONValidationException;
	IShellObject execute (IShellRequest request) throws ExtensionNotFound, ca.redleafsolutions.ishell2.IShellException.InvocationTargetException;
	IShellObject execute (IShellRequestSingle request) throws ExtensionNotFound, ca.redleafsolutions.ishell2.IShellException.InvocationTargetException;
	IShellObject execute (IShellRequestScript request);
	Map<String, Object> extensions ();
	void extend (String key, Object ext);
	void shrink (String key);
	void setDefaultRedirect (String redirect);
	String getPrompt ();
}

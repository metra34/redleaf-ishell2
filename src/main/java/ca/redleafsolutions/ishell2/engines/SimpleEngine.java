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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellException.BadParameters;
import ca.redleafsolutions.ishell2.IShellException.ExtensionNotFound;
import ca.redleafsolutions.ishell2.IShellException.IllegalFunctionOverload;
import ca.redleafsolutions.ishell2.IShellException.ResourceNotFound;
import ca.redleafsolutions.ishell2.IShellIntercept;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequest;
import ca.redleafsolutions.ishell2.IShellRequestConsumer;
import ca.redleafsolutions.ishell2.IShellRequestScript;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class SimpleEngine implements IShellEngine {
	protected ObjectMap extensions = new ObjectMap();
	private String redirect = null;

	public SimpleEngine() {
	}
	//
	// public SimpleEngine (JSONItem json) throws JSONValidationException {
	// getValidator ().validate (json);
	// }

	@Override
	public IShellObject execute(IShellRequest request)
			throws ExtensionNotFound, ca.redleafsolutions.ishell2.IShellException.InvocationTargetException {
		if (request instanceof IShellRequestSingle) {
			return execute((IShellRequestSingle) request);
		} else if (request instanceof IShellRequestScript) {
			return execute((IShellRequestScript) request);
		}
		throw new ClassCastException("Request of type " + request.getClass().getName() + " is not supported");
	}

	@Override
	public IShellObject execute(IShellRequestSingle request)
			throws ExtensionNotFound, ca.redleafsolutions.ishell2.IShellException.InvocationTargetException {
		Object root;
		if (request.getPath().size() <= 0) {
			root = this;
		} else {
			String element1 = request.getPath().get(0);
			if ("".equals(element1)) {
				root = this;
			} else {
				root = extensions.get(element1);
			}
		}
		if (root == null) {
			throw new IShellException.ExtensionNotFound(request);
		}

		IShellObject res;
		try {
			res = _execute(root, request, 1);
		} catch (Throwable e) {
			throw new IShellException.InvocationTargetException(request, e);
		}
		return res;
	}

	@Override
	public IShellObject execute(IShellRequestScript request) {
		throw new IShellException.NotImplementedYet();
	}

	@Override
	public void extend(JSONItem obj)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONValidationException {
		// JSONValidator extendValidator = new JSONValidator ();
		// extendValidator.put ("class", new StringValidator ().optional (false));
		// extendValidator.put ("params", new ClassValidator ().optional (true));

		for (int i = 0; i < obj.length(); ++i) {
			try {
				JSONItem item = obj.getJSON(i);

				// extendValidator.validate (item);

				String clss = item.getString("class");
				Class<?> cls = Class.forName(clss);

				Object instance = null;
				if (item.has("params")) {
					try {
						Object params = item.getJSON("params");
						Constructor<?> ctor = cls.getConstructor(JSONItem.class);
						instance = ctor.newInstance(params);
					} catch (JSONValidationException e) {
					}
				}

				if (instance == null) {
					Constructor<?> ctor = cls.getConstructor();
					instance = ctor.newInstance();
				}
				String key = item.getString("name");
				extend(key, instance);
			} catch (JSONValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private IShellObject _execute(Object res, IShellRequestSingle request, int index) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ResourceNotFound, BadParameters, IllegalFunctionOverload {
		if (res instanceof IShellRequestConsumer) {
			try {
				((IShellRequestConsumer) res).setRequest(request);
				if (res instanceof IShellIntercept) {
					return new IShellObject.RawObject(res, request);
				}
			} catch (Exception e) {
				iLogger.severe("Failed to set iShell Request to " + res.getClass().getSimpleName() + "due to " + e);
			}
		}

		if (request.getPath().size() <= index) {
			// for (Entry<String, Object> param: request.getParams ().entrySet ()) {
			// String key = param.getKey ();
			// try {
			// Field field = res.getClass ().getField (key);
			// assignField (field, param.getValue (), res);
			// } catch (NoSuchFieldException e) {
			// throw new IllegalAccessError (e.getMessage ());
			// }
			// }
			return new IShellObject.RawObject(res, request);
		}

		Object newres = null;
		String key = request.getPath().get(index);

		// special treatment for Map
		if (res instanceof Map) {
			newres = ((Map<? extends Object, ? extends Object>) res).get(key);
		} else {
			// special treatment for arrays
			try {
				int intkey = Integer.parseInt(key);
				if (res instanceof AbstractList) {
					AbstractList<? extends Object> castres = (AbstractList<? extends Object>) res;
					if (intkey < castres.size())
						newres = castres.get(intkey);
				} else if (res instanceof Iterable) {
					Iterable<? extends Object> castres = (Iterable<? extends Object>) res;
					int counter = 0;
					for (Object item : castres) {
						if (counter == intkey) {
							newres = item;
							break;
						}
						++counter;
					}
				}
			} catch (NumberFormatException e) {
				// if key is not integer, it's not an array accessor key
			}
		}

		if (newres == null) {
			Field[] fields = res.getClass().getFields();
			for (Field field : fields) {
				if (key.equalsIgnoreCase(field.getName())) {
					if (request.getPath().size() == index + 2) {
						// Type type = field.getGenericType ();
						// String typestr = type.toString ();
						String value = request.getPath().get(index + 1);
						assignField(field, value, res);
						return new IShellObject.ExecutedObject(field.get(res), request);
					} else {
						return new IShellObject.ExecutedObject(field.get(res), request);
					}
				}
			}
		}

		if (newres == null) {
			Method[] methods = res.getClass().getMethods();

			// filter all methods with name matches key
			Map<Integer, Method> matchMethods = new TreeMap<>();
			for (Method method : methods) {
				if (key.equalsIgnoreCase(method.getName())) {
					// make sure there's no more than one with the same number
					// of parameters
					int len = method.getParameterTypes().length;
					if (method.getAnnotation(IShellInvisible.class) == null) {
						if (!matchMethods.containsKey(len)) {
							matchMethods.put(len, method);
						} else {
							throw new IShellException.IllegalFunctionOverload(method);
						}
					}
				}
			}

			Object[] sortedMethodIndexes = matchMethods.keySet().toArray();
			Arrays.sort(sortedMethodIndexes, Collections.reverseOrder());

			// try and parse path parameters excluding methods with no
			// parameters
			IShellObject mfp = callMethodFromPath(index, request, matchMethods, res, newres);
			if (mfp != null) {
				return mfp;
			}

			// see if it can be extracted from URL parameters
			mfp = callMethodFromParams(index, request, matchMethods, res, newres, sortedMethodIndexes);
			if (mfp != null) {
				return mfp;
			}

			// try to call a method with no params at all
			mfp = callMethodNoParams(index, request, matchMethods, res, newres);
			if (mfp != null) {
				return mfp;
			}
		}
		if (newres == null) {
			throw new IShellException.ResourceNotFound(request.getPath().subList(0, index + 1));
		}
		return _execute(newres, request, index + 1);
	}

	private IShellObject callMethodFromParams(int index, IShellRequestSingle request, Map<Integer, Method> matchMethods,
			Object res, Object newres, Object[] sortedMethodIndexes) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ResourceNotFound, BadParameters, IllegalFunctionOverload {
		for (Object methodIndex : sortedMethodIndexes) {
			Method method = matchMethods.get(methodIndex);
			Class<?>[] paramtypes = method.getParameterTypes();
			if (paramtypes.length <= 0)
				return null;
			Object[] vals = new Object[paramtypes.length];
			boolean match = true;

			// try to read variabl names
			ParameterNames pnames = method.getAnnotation(ParameterNames.class);
			if (pnames != null) {
				int i = 0;
				for (String pname : pnames.value()) {
					Object pvalue = request.getParams().get(pname);
					if (pvalue != null) {
						Class<?> ptype = paramtypes[i];
						try {
							setParam(vals, i, pvalue, ptype);
						} catch (IllegalArgumentException e) {
							throw new IllegalArgumentException(
									"argument '" + pname + "' type mismatch. Expected File but get " + ptype);
						}
						++i;
					} else {
						match = false;
						break;
					}
				}
			} else {
				// if no variable names, try to see if there are integer
				// parameter IDs
				for (int i = 0; i < vals.length; ++i) {
					String paramkey = Integer.toString(i + 1);
					ObjectMap params = request.getParams();
					if (params.containsKey(paramkey)) {
						Object paramValue = params.get(paramkey);
						Class<?> ptype = paramtypes[i];
						try {
							setParam(vals, i, paramValue, ptype);
						} catch (Throwable e) {
							match = false;
							break;
						}
					} else {
						match = false;
						break;
					}
				}
			}
			if (match) {
				newres = method.invoke(res, vals);
				if (newres == null)
					return new IShellObject.ExecutedObject(null, request);

				if (request.getPath().size() > index + 1)
					return _execute(newres, request, index + 1);
				return new IShellObject.ExecutedObject(newres, request);
			}
		}
		return null;
	}

	private IShellObject callMethodFromPath(int index, IShellRequestSingle request, Map<Integer, Method> matchMethods,
			Object res, Object newres) throws BadParameters, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, ResourceNotFound, IllegalFunctionOverload {
		List<String> strparams = new ArrayList<>();
		Map<Method, Object[]> methodCandidates = new HashMap<>();

		for (int i = index + 1; i < request.getPath().size(); ++i) {
			strparams.add(request.getPath().get(i));
		}

		for (Method method : matchMethods.values()) {
			Class<?>[] paramtypes = method.getParameterTypes();
			// if (paramtypes.length == strparams.size ()) {
			if (paramtypes.length <= request.getPath().size() - index - 1) {
				Object[] vals = new Object[paramtypes.length];
				try {
					for (int i = 0; i < vals.length; ++i) {
						String valstr = request.getPath().get(index + i + 1);
						Class<?> ptype = paramtypes[i];
						setParam(vals, i, valstr, ptype);
					}
					if (vals.length > 0) {
						methodCandidates.put(method, vals);
					}
				} catch (RuntimeException e) {
				}
			}
		}

		// in case there are no suitable methods
		if ((methodCandidates.size() == 0) && (matchMethods.size() > 0)) {
			return null;
		}

		Entry<Method, Object[]> selectedEntry = null;
		// if only one candidate: it simple: just use it
		if (methodCandidates.size() == 1) {
			selectedEntry = methodCandidates.entrySet().iterator().next();
		} else if (methodCandidates.size() > 1) {
			// if more than one candidate, use the one with the most
			// parameters matchin
			int maxparams = 0;
			for (Entry<Method, Object[]> entry : methodCandidates.entrySet()) {
				if (maxparams < entry.getValue().length) {
					maxparams = entry.getValue().length;
					selectedEntry = entry;
				}
			}
		}

		if (selectedEntry != null) {
			Method method = selectedEntry.getKey();
			Object[] vals = selectedEntry.getValue();
			index += vals.length;
			newres = method.invoke(res, vals);
			if (newres == null)
				return new IShellObject.ExecutedObject(null, request);
			if (request.getPath().size() > index + 1) {
				return _execute(newres, request, index + 1);
			} else if (request.getPath().size() == index + 1) {
				return new IShellObject.ExecutedObject(newres, request);
			}
			return new IShellObject.ExecutedObject(newres, request);
		}

		return null;
	}

	private IShellObject callMethodNoParams(int index, IShellRequestSingle request, Map<Integer, Method> matchMethods,
			Object res, Object newres) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			ResourceNotFound, BadParameters, IllegalFunctionOverload {
		Method method = matchMethods.get(0);
		if (method == null)
			return null;
		newres = method.invoke(res, new Object[0]);
		if (newres == null)
			return new IShellObject.ExecutedObject(null, request);
		if (request.getPath().size() > index + 1) {
			return _execute(newres, request, index + 1);
		} else if (request.getPath().size() == index + 1) {
			if (newres instanceof IShellRequestConsumer) {
				try {
					((IShellRequestConsumer) newres).setRequest(request);
					if (newres instanceof IShellIntercept) {
						return new IShellObject.RawObject(newres, request);
					}
				} catch (Exception e) {
					iLogger.severe("Failed to set iShell Request to " + res.getClass().getSimpleName() + "due to " + e);
				}
			}
			return new IShellObject.ExecutedObject(newres, request);
		}
		return new IShellObject.ExecutedObject(newres, request);
	}

	/**
	 * @param vals
	 * @param ind
	 * @param paramvalue
	 * @param paramType
	 */
	private void setParam(Object[] vals, int ind, Object paramvalue, Class<?> paramType) {
		String ptype = paramType.toString();
		String pvalue = paramvalue.toString();
		if (paramType.isPrimitive()) {
			if ("boolean".equalsIgnoreCase(ptype)) {
				vals[ind] = Boolean.parseBoolean(pvalue);
			} else if ("byte".equalsIgnoreCase(ptype)) {
				vals[ind] = Byte.parseByte(pvalue);
			} else if ("char".equalsIgnoreCase(ptype)) {
				vals[ind] = pvalue.charAt(0);
			} else if ("double".equalsIgnoreCase(ptype)) {
				vals[ind] = Double.parseDouble(pvalue);
			} else if ("float".equalsIgnoreCase(ptype)) {
				vals[ind] = Float.parseFloat(pvalue);
			} else if ("int".equalsIgnoreCase(ptype)) {
				vals[ind] = Integer.parseInt(pvalue);
			} else if ("long".equalsIgnoreCase(ptype)) {
				vals[ind] = Long.parseLong(pvalue);
			} else if ("short".equalsIgnoreCase(ptype)) {
				vals[ind] = Short.parseShort(pvalue);
			} else {
				vals[ind] = paramvalue;
			}
		} else {
			if (paramvalue instanceof File) {
				if (!paramType.isAssignableFrom(File.class)) {
					throw new IllegalArgumentException("argument type mismatch. Expected File but get " + paramType);
				}
			} else if (paramType.isAssignableFrom(JSONItem.class)) {
				try {
					if (paramvalue instanceof String) {
						paramvalue = JSONItem.parse((String) paramvalue);
					} else if (paramvalue instanceof JSONWritable) {
						paramvalue = ((JSONWritable) paramvalue).toJSON();
					}
				} catch (JSONValidationException e) {
					// leave it for later exception handling
				}
			} else if (Date.class.isAssignableFrom(paramType)) {
				try {
					paramvalue = new Date(Long.parseLong(pvalue));
				} catch (NumberFormatException e) {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					try {
						paramvalue = df.parse(pvalue);
					} catch (ParseException e1) {
						df = new SimpleDateFormat("yyyyMMdd");
						try {
							paramvalue = df.parse(pvalue);
						} catch (ParseException e2) {
							throw new IllegalArgumentException("Argument type mismatch. Expected Date but got " + pvalue);
						}
					}
				}
			}
			vals[ind] = paramvalue;
		}
	}

	private void assignField(Field field, Object value, Object res)
			throws IllegalArgumentException, IllegalAccessException {
		Type type = field.getGenericType();
		String typestr = type.toString();

		if ("File".equalsIgnoreCase(typestr)) {
			if (value instanceof File) {
				field.set(res, value);
				return;
			}
			throw new IllegalAccessException("File type is not assignable from " + value.getClass().getName());
		} else if ("Date".equalsIgnoreCase(typestr)) {
			if (value instanceof Date) {
				field.set(res, value);
				return;
			}
			throw new IllegalAccessException("Date type is not assignable from " + value.getClass().getName());
		} else if ("JSONItem".equalsIgnoreCase(typestr)) {
			if (value instanceof String) {
				try {
					value = JSONItem.parse((String) value);
				} catch (JSONValidationException e) {
					throw new IllegalAccessException("JSONItem failed to assign due to " + e);
				}
			}
			if (value instanceof JSONItem) {
				field.set(res, value);
				return;
			} else if (value instanceof JSONWritable) {
				try {
					field.set(res, ((JSONWritable) value).toJSON());
				} catch (JSONValidationException e) {
					throw new IllegalAccessException("JSONItem " + field.getName() + " failed to assign due to " + e);
				}
				return;
			}
			throw new IllegalAccessException("JSONItem type is not assignable from " + value.getClass().getName());
		}

		String sval = (String) value;
		if ("boolean".equalsIgnoreCase(typestr)) {
			Boolean result = Boolean.valueOf(sval);
			field.setBoolean(res, result);
		} else if ("byte".equalsIgnoreCase(typestr)) {
			Byte result = Byte.valueOf(sval);
			field.setByte(res, result);
		} else if ("char".equalsIgnoreCase(typestr)) {
			Character result = Character.valueOf(sval.charAt(0));
			field.setChar(res, result);
		} else if ("double".equalsIgnoreCase(typestr)) {
			Double result = Double.valueOf(sval);
			field.setDouble(res, result);
		} else if ("float".equalsIgnoreCase(typestr)) {
			Float result = Float.valueOf(sval);
			field.setFloat(res, result);
		} else if ("int".equalsIgnoreCase(typestr)) {
			Integer result = Integer.valueOf(sval);
			field.setInt(res, result);
		} else if ("long".equalsIgnoreCase(typestr)) {
			Long result = Long.valueOf(sval);
			field.setLong(res, result);
		} else if ("short".equalsIgnoreCase(typestr)) {
			Short result = Short.valueOf(sval);
			field.setShort(res, result);
		} else if ("class java.lang.String".equalsIgnoreCase(typestr)) {
			field.set(res, value);
		} else {
			throw new IllegalAccessException("Type " + typestr + " is not assignable from string");
		}
	}

	@Override
	public Map<String, Object> extensions() {
		return this.extensions;
	}

	@IShellInvisible
	@Override
	public void extend(String key, Object ext) {
		iLogger.info("iShell extending " + key + " => " + ext.getClass().getName());
		this.extensions.put(key, ext);
	}

	@Override
	public void shrink(String key) {
		this.extensions.remove(key);
	}

	@IShellInvisible
	@Override
	public String toString() {
		return "Lite Engine";
	}
	//
	// @Override
	// public JSONValidator getValidator () {
	// JSONValidator validator = new JSONValidator ();
	// validator.put ("int", new IntegerValidator ());
	// return validator;
	// }

	@Override
	@IShellInvisible
	public void setDefaultRedirect(String redirect) {
		this.redirect = redirect;
	}

	@Override
	public String getUrl() {
		return redirect;
	}

	@Override
	public String getPrompt() {
		return "> ";
	}
}

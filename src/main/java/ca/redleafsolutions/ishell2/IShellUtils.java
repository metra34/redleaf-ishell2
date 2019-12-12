package ca.redleafsolutions.ishell2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadable;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellUtils {
	@SuppressWarnings ("unchecked")
	public static <T> T classFromJSON (JSONItem json, Class<T> type) throws JSONValidationException {
		String classname = json.getString ("class");

		Class<? extends T> cls = null;
		try {
			cls = (Class<? extends T>)Class.forName (classname);
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new JSONValidationException.IllegalValue ("class", classname);
		}

		JSONItem params = null;
		try {
			params = json.getJSON ("params");
		} catch (JSONValidationException.MissingKey e2) {
		}

		T o = null;
		Constructor<? extends T> ctor = null;
		try {
			if (params != null) {
				try {
					ctor = cls.getConstructor (JSONItem.class);
					o = ctor.newInstance (params);
				} catch (NoSuchMethodException e) {
					// do nothing, next block will try default constructor
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new JSONValidationException (e);
				}
			}

			if (o == null) {
				try {
					ctor = cls.getConstructor ();
					o = ctor.newInstance ();
					if (params != null) {
						if (o instanceof JSONReadable)
							((JSONReadable)o).fromJSON (params);
					}
				} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
					throw new JSONValidationException (e);
				}
			}
		} catch (SecurityException e) {
			throw new JSONValidationException (e);
		}
		return o;
	}
//
//	public static <T> T classByName (String classname, Class<T> type) throws InstantiationException {
//		try {
//			@SuppressWarnings ("unchecked")
//			Class<? extends T> cls = (Class<? extends T>)Class.forName (classname);
//			Constructor<? extends T> ctor = cls.getConstructor ();
//			return ctor.newInstance ();
//		} catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
//			throw new InstantiationException (e.toString ());
//		}
//	}
}

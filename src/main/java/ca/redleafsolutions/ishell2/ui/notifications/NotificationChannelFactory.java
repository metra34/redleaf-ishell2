package ca.redleafsolutions.ishell2.ui.notifications;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ca.redleafsolutions.SingletonException;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.ui.notifications.javaws.NotificationChannelJavaWS;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class NotificationChannelFactory  {
	static private NoticiationChannelBase instance;

	@IShellInvisible
	static public NoticiationChannelBase getInstance () {
		return instance;
	}
	
	public NotificationChannelFactory () throws SingletonException, JSONValidationException {
		if (instance != null) {
			throw new SingletonException(this);
		}
		instance = new NotificationChannelJavaWS ();	
	}
	
	public NotificationChannelFactory (JSONItem json) throws SingletonException, JSONValidationException {
		if (instance != null) {
			throw new SingletonException(this);
		}

		try {
			String type = json.getString ("type");
			if ("websocket".equals (type)) {
				instance = new NotificationChannelJavaWS (json);	
			} else {
				try {
					@SuppressWarnings ("unchecked")
					Class<? extends NoticiationChannelBase> cls = (Class<? extends NoticiationChannelBase>)Class.forName (type);
					Constructor<? extends NoticiationChannelBase> ctor = cls.getConstructor (JSONItem.class);
					instance = ctor.newInstance (json);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
						| IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
				}
			}
		} catch (JSONValidationException e) {
		}
		
		if (instance == null) {
			instance = new NotificationChannelJavaWS (json);	
		}
	}
}

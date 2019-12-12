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

import ca.redleafsolutions.base.events.Event;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONWritable;

public class IShellEvent implements Event, JSONWritable {
	public static class Update extends IShellEvent {
		private Object o;

		public Update (Object o) {
			this.o = o;
		}

		public Object getObject () {
			return o;
		}
	}

	public static class ShellCreationComplete extends IShellEvent {
	}

	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newObject ();
		json.put ("type", "event");
		json.put ("class", this.getClass ().toString ());
		return json;
	}
}

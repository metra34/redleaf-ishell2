package ca.redleafsolutions;

import java.util.LinkedList;

import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONReadWritable;
import ca.redleafsolutions.json.JSONValidationException;

@SuppressWarnings ("serial")
public class JSONWritableList<T extends JSONReadWritable> extends LinkedList<T> implements JSONReadWritable {
	public JSONWritableList () {
		super ();
	}

	public JSONWritableList (JSONItem.Array json) throws JSONValidationException {
		fromJSON (json);
	}

	@Override
	public String toString () {
		try {
			return toJSON ().toString ();
		} catch (JSONValidationException e) {
			return super.toString ();
		}
	}
	
	@Override
	public JSONItem toJSON () throws JSONValidationException {
		JSONItem json = JSONItem.newArray ();
		for (T value: this) {
			json.put (value.toJSON ());
		}
		return json;
	}

	@Override
	public void fromJSON (JSONItem json) throws JSONValidationException {
		for (int i = 0; i < json.length (); ++i) {
			@SuppressWarnings ("unchecked")
			T o = (T)json.get (i);
			this.add (o);
		}
	}

	public String join (String string) {
		String s = "";
		for (Object item:this) {
			if (s.length () > 0) s += ",";
			s += item;
		}
		return s;
	}
}

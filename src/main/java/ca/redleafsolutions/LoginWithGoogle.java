package ca.redleafsolutions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ca.redleafsolutions.ishell2.HTMLWritable;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class LoginWithGoogle implements HTMLWritable {
	private ObjectMap map;

	public LoginWithGoogle (JSONItem json) throws JSONValidationException {
		map = new ObjectMap ();
		map.put ("clientid", json.getString ("clientid"));
		map.put ("apikey", json.getString ("apikey"));
		JSONItem functions = json.getJSON ("functions");
		map.put ("function-login", functions.getString ("login"));
		map.put ("function-logout", functions.getString ("logout"));
		JSONItem callback = json.getJSON ("callback");
		map.put ("callback-path", callback.getString ("path"));
		map.put ("callback-param", callback.getString ("param"));
	}

	@Override
	public String toString () {
		InputStream is = null;
		StringWriter writer = new StringWriter ();
		try {
			is = new ResourceLocator (this.getClass ()).getInputStream ("LoginWithGoogle.js");
			Velocity.evaluate (new VelocityContext (map), writer, "login-with-google", new InputStreamReader (is));
		} catch (Exception e) {
			return e.toString ();
		} finally {
			try {
				if (is != null)
					is.close ();
			} catch (IOException e) {
				return e.toString ();
			}
		}
		return writer.toString ();
	}

	@Override
	public String toHTML () {
		return this.toString ();
	}

	@MethodDescription ("Just a placeholder to try confirmation hook")
	@ParameterNames ("q")
	public JSONItem confirm (String q) throws JSONValidationException {
		return JSONItem.parse (q);
	}
}

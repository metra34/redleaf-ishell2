package ca.redleafsolutions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class TemplateUtils {
	static public Set<String> listVariables (String template) {
		Set<String> list = new HashSet<String> ();

		boolean done = false;
		int pstart = 0;
		while (!done) {
			done = true;
			int popen = template.indexOf ("${", pstart);
			if (popen >= pstart) {
				popen += 2;
				int pclose = template.indexOf ("}", popen);
				if (pclose > popen) {
					list.add (template.substring (popen, pclose));
					done = false;
				}
			}
		}

		for (String key:list) {
			if (key.indexOf (".") > 0) {
				String[] lst = key.split ("\\.");
				if (list.contains (lst[0])) {
					list.remove (key);
				}
			}
		}
		return list;
	}

	static public String evaluate (InputStream is, ObjectMap map, String logEntry) {
		try {
			if (logEntry == null)
				logEntry = "iShellTemplate";
			Context context = new VelocityContext (map);
			Writer out = new StringWriter ();
			Velocity.evaluate (context, out, logEntry, new InputStreamReader (is));
			return out.toString ();
		} finally {
			try {
				is.close ();
			} catch (IOException e) {
				// attempt to close stream as it was completely read
			}
		}
	}

	static public String evaluate (InputStream is, ObjectMap map) {
		if (is == null)
			return "";
		return evaluate (is, map, null);
	}

	static public String evaluate (String template, ObjectMap map, String logEntry) {
		if (logEntry == null)
			logEntry = "iShellTemplate";
		Context context = new VelocityContext (map);
		Writer out = new StringWriter ();
		Velocity.evaluate (context, out, logEntry, template);
		return out.toString ();
	}

	static public String evaluate (String template, ObjectMap map) {
		return evaluate (template, map, null);
	}
}

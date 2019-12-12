package ca.redleafsolutions;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class Trace {
	private static boolean enable = false;
	public static boolean traceSource = true;
	public static boolean traceTime = true;
	public static boolean traceNanoTime = true;
	public static boolean traceTimeOfDay = false;
	public static boolean traceThread = false;

//	private static DecimalFormat formatter = new DecimalFormat ("#,###");
	private static SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss.SSS");
	
	public Trace () {
	}
	public Trace (JSONItem json) {
		try {
			Trace.enable = json.getBoolean ("on");
		} catch (JSONValidationException e) {
			Trace.enable = false;
		}
		try {
			Trace.traceThread = json.getBoolean ("thread");
		} catch (JSONValidationException e) {
			Trace.traceThread = false;
		}
	}
	
	@MethodDescription ("Turn tracing on (visible)")
	public static synchronized void on () {
		enable = true;
	}

	@MethodDescription ("Turn tracing off (invisible)")
	public static synchronized void off () {
		enable = false;
	}

	@MethodDescription ("Check tracing state on or off")
	public static synchronized boolean state () {
		return enable;
	}

	@MethodDescription ("Trace info level messages")
	@ParameterNames ("message")
	@ParameterDescriptions ("Message to be traced")
	public static synchronized void info (String str) {
		_logger (str, 1);
	}
	public static synchronized void info (Object... str) {
		_logger (StringUtils.join (str, " "), 1);
	}

	@IShellInvisible
	public static synchronized void logger (String str) {
		_logger (str, 2);
	}

	private static synchronized void _logger (String str, int level) {
		if (!enable)
			return;
		if (traceTimeOfDay) {
			Calendar now = Calendar.getInstance ();
			System.out.print (now.get (Calendar.HOUR) + ":" + now.get (Calendar.MINUTE) + ":" + now.get (Calendar.SECOND) + ":"
					+ now.get (Calendar.MILLISECOND) + ":");
		} else if (traceTime) {
			System.out.print (dateformat.format (Calendar.getInstance ().getTime ()));
			if (traceNanoTime)
				System.out.print ("/" + (System.nanoTime () % 1000000));
			System.out.print (": ");
		}
		if (traceThread)
			System.out.print (Thread.currentThread ().getName () + "/");
		if (traceSource) {
			StackTraceElement callFrom = new Exception ().getStackTrace ()[level+1];
			// String[] method = callFrom.getClassName ().split (".");
			String clsname = callFrom.getClassName ();
			clsname = clsname.substring (clsname.lastIndexOf ('.') + 1);
			System.out.print (clsname + "." + callFrom.getMethodName () + "(" + callFrom.getLineNumber () + ")>");
		}
		System.out.println (str);
	}

	@Override
	public String toString () {
		return "Trace is " + (enable? "on": "off");
	}
}

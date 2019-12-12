/*
 * iShell 2.0
 *
 * Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
 *
 * This library is proprietary software; you can not redistribute
 * without an explicit consent from Releaf Solutions Ltd.
 * The consent will detail the distribution and sale rights.
 */

package ca.redleafsolutions.ishell2.logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang.StringUtils;

import ca.redleafsolutions.Nagger;
import ca.redleafsolutions.NaggerVictim;
import ca.redleafsolutions.ObjectList;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.Trace;
import ca.redleafsolutions.ishell2.IShellInputStream;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.SystemWrapper;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.ui.HTMLUI;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class iLogger extends Formatter implements NaggerVictim {

	protected final static Logger logger = Logger.getGlobal ();
	SimpleDateFormat time_format = new SimpleDateFormat ("HH:mm:ss.SSS");
	private String dir = null;
	private String format = null;
	public boolean details = false;
	static private boolean trace = false;
	static private boolean traceTurnedOn = false;
	private int startday;
	private Handler filehandler;
	static private LevelIShellRequest levelIShellRequest = new LevelIShellRequest ();
	static private LevelIShellResponse levelIShellResponse = new LevelIShellResponse ();
	private static int requestCounter = 0;

	private List<String> packagesOfInterest;

	public iLogger () {
		this (JSONItem.newObject ());
	}

	public iLogger (JSONItem json) {
		try {
			dir = json.getString ("dir");
		} catch (JSONValidationException e) {
		}
		try {
			format = json.getString ("format");
		} catch (JSONValidationException e) {
		}

		logger.setUseParentHandlers (false);

		try {
			String level = json.getString ("level");
			logger.setLevel (Level.parse (level));
		} catch (JSONValidationException e) {
			logger.setLevel (Level.INFO);
		}

		try {
			trace = json.getBoolean ("trace");
		} catch (JSONValidationException e) {
			trace = false;
		}
		traceTurnedOn = false;

		packagesOfInterest = new LinkedList<String> ();
		try {
			JSONItem jpkg = json.getJSON ("packages");
			for (int i=0; i < jpkg.length (); ++i) {
				packagesOfInterest.add (jpkg.getString (i));
			}
		} catch (JSONValidationException e) {
			packagesOfInterest = null;
		}

		new Nagger (this, "Logger restart service").execute ().start ();
	}

	public Logger global () {
		return Logger.getGlobal ();
	}

	public Logger anonymous () {
		return Logger.getAnonymousLogger ();
	}

	public Logger get (String name) {
		return Logger.getLogger (name);
	}

	public void trace (boolean trace) {
		iLogger.trace = trace;
		if (trace) {
			if (!Trace.state ()) {
				traceTurnedOn = true;
				Trace.on ();
			}
		} else if (traceTurnedOn) {
			Trace.off ();
		}
	}
	public boolean trace () {
		return trace;
	}
	
	public String info () {
		String s = "";
		s += "Name: " + logger.getName ();
		s += "\nClass: " + logger.getClass ();
		s += "\nLevel: " + logger.getLevel ();
		s += "\nResource Bundle Name: " + logger.getResourceBundleName ();
		s += "\nParent: " + logger.getParent ();
		s += "\nUse Parent Handlers: " + logger.getUseParentHandlers ();
		s += "\nHandlers:";
		for (Handler handler: logger.getHandlers ()) {
			s += "\n\t- " + handler;
		}
		return s;
	}

	public void file (String filename) throws SecurityException, IOException {
		file (filename, null);
	}

	public void file (String dirname, String format) throws SecurityException, IOException {
		File dir = new File (dirname);
		if (dir.exists ()) {
			if (!dir.isDirectory ()) {

			}
		} else {
			dir.mkdirs ();
		}

		SimpleDateFormat date_format = new SimpleDateFormat ("yyyyMMdd");
		File file = new File (dir, date_format.format (new Date (System.currentTimeMillis ())) + ".log");
		FileHandler loghandler = new FileHandler (file.getAbsolutePath (), true);
		logger.addHandler (loghandler);

		if ("xml".equalsIgnoreCase (format)) {
		} else if ("simple".equalsIgnoreCase (format)) {
			loghandler.setFormatter (new SimpleFormatter ());
			// } else if ("http".equalsIgnoreCase (format)) {
			// logfile.setFormatter (new HttpLogFormatter ());
		} else {
			loghandler.setFormatter (this);
		}
		iLogger.info ("Creating new log file " + file.getAbsolutePath () + " with an "
				+ (format == null ? this.getClass ().getSimpleName () : format) + " formatter");
		SystemWrapper sys = new SystemWrapper ();
		iLogger.info ("OS: " + sys.os ());
		iLogger.info ("Memory: " + sys.mem ());
		iLogger.info ("Network: " + sys.network ());

		if (filehandler != loghandler) {
			if (filehandler != null) {
				iLogger.info ("Killing old log file");
				logger.removeHandler (filehandler);
			}
			filehandler = loghandler;
		}
	}

	public static void level (String level) {
		// logger.setLevel (Level.ALL);
		// logger.setLevel (Level.CONFIG);
		// logger.setLevel (Level.FINE);
		// logger.setLevel (Level.FINER);
		// logger.setLevel (Level.FINEST);
		// logger.setLevel (Level.INFO);
		// logger.setLevel (Level.OFF);
		// logger.setLevel (Level.SEVERE);
		// logger.setLevel (Level.WARNING);
		logger.setLevel (Level.parse (level.toUpperCase ()));
	}

	public static Map<Integer, String> levels () {
		Map<Integer, String> levels = new TreeMap<> ();
		for (Field field: Level.class.getFields ()) {
			String name = field.getName ();
			Level level = Level.parse (name);
			levels.put (level.intValue (), name);
		}
		levels.put (levelIShellRequest.intValue (), levelIShellRequest.getName ());
		levels.put (levelIShellResponse.intValue (), levelIShellResponse.getName ());
		return levels;
	}

	public static String level () {
		return logger.getLevel ().getName ();
	}

	public static int levelint () {
		Level level = logger.getLevel ();
		if (level != null)
			return level.intValue ();
		return Level.SEVERE.intValue ();
	}

	public static void severe (String msg) {
		logger.severe (msg);
		if (!trace) {
			Trace.info (msg);
		}
	}

	public static void severe (Throwable e) {
		if (levelint () <= Level.FINER.intValue ()) {
			StackTraceElement[] stacktrace = e.getStackTrace ();
			String s = e.toString () + "\n\tThread: " + Thread.currentThread ().getName () + " ("
					+ Thread.currentThread ().getId () + ")";
			for (int i = 0; i < stacktrace.length; ++i) {
				s += "\n\t" + stacktrace[i].getClassName () + ":" + stacktrace[i].getMethodName () + " () in file "
						+ stacktrace[i].getFileName () + " line " + stacktrace[i].getLineNumber ();
			}
			severe (s);
		} else {
			severe (e.toString ());
		}
	}

	public static void warning (String msg) {
		logger.warning (msg);
	}

	public static void info (String msg) {
		logger.info (msg);
	}

	public static void fine (String msg) {
		logger.fine (msg);
	}

	public static void finer (String msg) {
		logger.finer (msg);
	}

	public static void finest (String msg) {
		logger.finest (msg);
	}

	@IShellInvisible
	@Override
	public String format (LogRecord record) {
		String s = System.getProperty ("line.separator") + time_format.format (new Date (record.getMillis ()));
		s += " [" + record.getLevel ();
		
		// if set to output details
		if (this.details) {
			s += "|" + Thread.currentThread ().getName () + "/" + Thread.currentThread ().getId ();
			
			if (packagesOfInterest != null) {
				StackTraceElement[] stack = new Exception ().getStackTrace ();
				for (int i = 0; i < stack.length; ++i) {
					StackTraceElement stackElement = stack[i];
					String clsname = stackElement.getClassName ();
					for (String pkg: packagesOfInterest) {
						if (!iLogger.class.getName ().equals (clsname) && (clsname.indexOf (pkg) == 0)) {
							clsname = clsname.substring (clsname.lastIndexOf ('.') + 1);
							s += "|" + clsname + "." + stackElement.getMethodName () + "(" + stackElement.getLineNumber ()
									+ ")";
							i = stack.length;
							break;
						}
					}
				}
			}
		}

		s += "] ";
		if (record.getMessage ().length () > 1024) {
			s += record.getMessage ().substring (0, 1024) + "...";
		} else {
			s += record.getMessage ();
		}
		
		// if trace logs on console: output to stdout
		if (trace)
			System.out.print (s);
		
		return s;
	}

	public static synchronized int logIShellRequest (IShellRequestSingle request) {
		if (!logger.isLoggable (levelIShellRequest))
			return -1;
		++requestCounter;
		logger.log (levelIShellRequest, requestCounter + ": " + request.toURL ());
		return requestCounter;
	}

	public static synchronized int logIShellRequest (IShellRequestSingle request, InetAddress address) {
		if (!logger.isLoggable (levelIShellRequest))
			return -1;
		++requestCounter;
		logger.log (levelIShellRequest,
				requestCounter + " <" + (address.isLoopbackAddress () ? "127.0.0.1" : address.getHostAddress ()) + "> "
						+ request.toURL ());
		return requestCounter;
	}

	public static synchronized void logIShellResponse (int reqid, String... str) {
		if ((str == null) || (str.length <= 0))
			return;

		if (!logger.isLoggable (levelIShellResponse))
			return;

		String s = StringUtils.join (str);
		int pos = s.indexOf ("\n");
		if (pos < 0)
			pos = s.length ();
		pos = Math.min (pos, 256);
		logger.log (levelIShellResponse,
				reqid + ": " + s.substring (0, pos) + ((pos < s.length ()) ? "... (size " + s.length () + ")" : ""));
	}

	public static synchronized void logIShellResponse (Throwable e) {
		if (!logger.isLoggable (levelIShellResponse))
			return;
		logger.log (levelIShellResponse, e.toString ());
	}

	@IShellInvisible
	@Override
	public void again () {
		int today = Calendar.getInstance ().get (Calendar.DAY_OF_YEAR);
		if (today != startday) {
			if (dir != null) {
				try {
					if (format != null) {
						file (dir, format);
					} else {
						file (dir);
					}
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace ();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace ();
				}
			}
			startday = today;
		}
	}

	@IShellInvisible
	@Override
	public void waitCondition () {
		// calculate how much time left to end of day
		int h = Calendar.getInstance ().get (Calendar.HOUR_OF_DAY);
		int m = Calendar.getInstance ().get (Calendar.MINUTE);
		int s = Calendar.getInstance ().get (Calendar.SECOND);
		int day = 24 * 60 * 60 * 1000; // milliseconds in a 24 hour period
		int millis = Math.max (((h * 60 + m) * 60 + s), 1) * 1000;	// make sure millis >= 1000
		iLogger.info ("iLogger daily file refresh within " + ((day - millis) / 1000) + " seconds ("
				+ (((day - millis) / 360000) / 10.) + "h). Mem "
				+ String.format ("%.2f", (Runtime.getRuntime ().freeMemory () / 1048576.)) + " MB, " + Thread.activeCount () + " threads");

		millis = Math.min (millis, 10 * 60 * 1000); // max: 10 min
		try {
			Thread.sleep (millis);
		} catch (InterruptedException e) {
			handleNaggerException (e);
		}
	}

	@IShellInvisible
	@Override
	public boolean done () {
		return false;
	}

	@Override
	public void handleNaggerException (Throwable e) {
		iLogger.severe (e);
	}

	static private class LevelIShellRequest extends Level {
		private static final long serialVersionUID = 5779312073512644543L;

		protected LevelIShellRequest () {
			super ("REQUEST", (Level.WARNING.intValue () + Level.INFO.intValue ()) / 2 - 1);
		}
	}

	static private class LevelIShellResponse extends Level {
		private static final long serialVersionUID = 6457312975405110595L;

		protected LevelIShellResponse () {
			super ("RESPONSE", (Level.WARNING.intValue () + Level.INFO.intValue ()) / 2 + 1);
		}
	}
	
	public ObjectList list () {
		ObjectList list = new ObjectList ();
		for (File file:new File (dir).listFiles ()) {
			if (file.isFile () && (file.length () > 0) && (file.getName ().indexOf (".lck") < 0)) {
				list.add (file);
			}
		}
		list.sort (new Comparator<Object>() {
			@Override
			public int compare (Object o1, Object o2) {
				if ((o1 instanceof File) && (o2 instanceof File)) {
					File f1 = (File)o1;
					File f2 = (File)o2;
					return -f1.compareTo (f2);
				};
				return 0;
			}});
		return list;
	}
	
	public IShellInputStream read (String logname) throws FileNotFoundException {
		File dir = new File (this.dir);
		File file = new File (dir, logname);
		return new IShellInputStream (new FileInputStream (file), file.length ());
	}
	
	public HTMLUI filter () throws IOException {
		ObjectMap map = new ObjectMap ();
		map.put ("logfiles", list ());
		return new HTMLUI (this.getClass (), "logviewer.html", map);
	}
}

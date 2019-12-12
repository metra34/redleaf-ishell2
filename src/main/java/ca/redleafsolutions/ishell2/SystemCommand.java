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

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ca.redleafsolutions.ObjectList;

public class SystemCommand {
	public List<String> env () {
		Properties props = System.getProperties ();
		List<String> list = new LinkedList<String> ();
		for (Object key:props.keySet ()) {
			Object value = props.get (key);
			list.add (key + " = " + value);
		}
		Collections.sort (list);
		return list;
	}

	public String env (String key) {
		return System.getProperty (key);
	}

	public void env (String key, String value) {
		System.setProperty (key, value);
	}

	public void gc () {
		System.gc ();
	}

	public long milli () {
		return System.currentTimeMillis ();
	}

	public long freemem () {
		return Runtime.getRuntime ().freeMemory ();
	}

	public long maxmem () {
		return Runtime.getRuntime ().maxMemory ();
	}

	public long totmem () {
		return Runtime.getRuntime ().totalMemory ();
	}
	
	public ObjectList memory () {
		ObjectList memorymap = new ObjectList ();
		Iterator<MemoryPoolMXBean> iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
		while (iter.hasNext()) {
		    MemoryPoolMXBean item = iter.next();
		    String name = item.getName();
		    MemoryType type = item.getType();
		    MemoryUsage usage = item.getUsage();
		    MemoryUsage peak = item.getPeakUsage();
		    MemoryUsage collections = item.getCollectionUsage();
		    memorymap.add (name + ": " + type + " - usage " + usage + ", peak " + peak + ", collections " + collections);
		}
		return memorymap;
	}

	public void execnoresult (String cmd) throws IOException {
		Runtime.getRuntime ().exec (cmd);
	}

	public String exec (String cmd) throws IOException {
		Process proc = Runtime.getRuntime ().exec (cmd);
		InputStream is = proc.getInputStream ();
		String result = "";
		byte[] buff = new byte[512];
		int len = 1;
		while (len >= 0) {
			len = is.read (buff);
			if (len > 0) {
				result += new String (buff, 0, len);
			}
		}
		return result;
	}
//
//	public void traceon () {
//		Runtime.getRuntime ().traceInstructions (true);
//		Runtime.getRuntime ().traceMethodCalls (true);
//	}
//
//	public void traceoff () {
//		Runtime.getRuntime ().traceInstructions (false);
//		Runtime.getRuntime ().traceMethodCalls (false);
//	}

	public Date time () {
		return Calendar.getInstance ().getTime ();
	}

	public Thread[] threads () {
		Thread[] threads = new Thread[Thread.activeCount ()];
		return threads;
	}

	public List<String> classpath () {
		return Arrays.asList (System.getProperty ("java.class.path").split (";"));
	}

	public URL url (String cls) {
		cls = cls.replace (".", "/") + ".class";
		ClassLoader cl = this.getClass ().getClassLoader ();
		URL url = cl.getResource (cls);
		return url;
	}
}

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.swing.filechooser.FileSystemView;

import ca.redleafsolutions.ObjectList;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;

public class SystemWrapper {
	static long startTime = System.nanoTime ();

	public Map<String, String> env () {
		return System.getenv ();
	}

	public String env (String name) {
		return System.getenv (name);
	}

	public Properties props () {
		return System.getProperties ();
	}

	public String prop (String key) {
		return System.getProperty (key);
	}

	public void load (String filename) {
		System.load (filename);
	}

	public void loadlib (String libname) {
		System.loadLibrary (libname);
	}

	public long freemem () {
		return Runtime.getRuntime ().freeMemory ();
	}

	public int processors () {
		return Runtime.getRuntime ().availableProcessors ();
	}

	static public Collection<String> mac () throws SocketException {
		Collection<String> list = new LinkedList<> ();
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces ();
		for (NetworkInterface netint:Collections.list (nets)) {
			byte[] mac = netint.getHardwareAddress ();
			if (mac != null) {
				String s = "";
				int total = 0;
				for (byte b:mac) {
					if (s.length () > 0)
						s += ":";
					int i = b < 0? 256 + b: b;
					if (i < 0x10)
						s += 0;
					s += Integer.toString (i, 16);
					total += i;
				}
				if (total > 0) {
					list.add (s);
				}
			}
		}
		return list;
	}

	public Collection<Map<String, Object>> network () throws SocketException {
		Collection<Map<String, Object>> list = new LinkedList<> ();
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces ();
		for (NetworkInterface netint:Collections.list (nets)) {
			Map<String, Object> map = new HashMap<> ();
			list.add (map);
			map.put ("displayname", netint.getDisplayName ());
			map.put ("MAC", Arrays.toString (netint.getHardwareAddress ()));
			map.put ("index", netint.getIndex ());
			map.put ("addresses", netint.getInterfaceAddresses ());
			map.put ("MTU", netint.getMTU ());
			map.put ("name", netint.getName ());
		}
		return list;
	}

	public Map<String, Object> runtime () {
		Map<String, Object> map = new HashMap<> ();
		RuntimeMXBean mxb = ManagementFactory.getRuntimeMXBean ();
		map.put ("classpath", mxb.getClassPath ());
		map.put ("input-args", mxb.getInputArguments ());
		map.put ("lib-path", mxb.getLibraryPath ().split (":"));
		map.put ("mng-spec-ver", mxb.getManagementSpecVersion ());
		map.put ("name", mxb.getName ());
		map.put ("spec-name", mxb.getSpecName ());
		map.put ("spec-vendor", mxb.getSpecVendor ());
		map.put ("spec-ver", mxb.getSpecVersion ());
		map.put ("starttime", mxb.getStartTime ());
		map.put ("uptime", mxb.getUptime ());
		map.put ("properties", mxb.getSystemProperties ());
		map.put ("vm-name", mxb.getVmName ());
		map.put ("vm-vendor", mxb.getVmVendor ());
		map.put ("vm-ver", mxb.getVmVersion ());
		map.put ("boot-classpath", mxb.getBootClassPath ().split (":"));
		return map;
	}

	public Map<String, Object> drives () {
		Map<String, Object> map = new HashMap<> ();
		FileSystemView fsv = FileSystemView.getFileSystemView ();
		map.put ("default-dir", fsv.getDefaultDirectory ());
		map.put ("home-dir", fsv.getHomeDirectory ());
		for (File root:File.listRoots ()) {
			Map<String, Object> dmap = new HashMap<> ();
			map.put (root.getAbsolutePath (), dmap);
			dmap.put ("name", fsv.getSystemDisplayName (root));
			dmap.put ("description", fsv.getSystemTypeDescription (root));
			dmap.put ("floppy", fsv.isFloppyDrive (root));
			dmap.put ("total", root.getTotalSpace ());
			dmap.put ("free", root.getFreeSpace ());
			dmap.put ("usable", root.getUsableSpace ());
			dmap.put ("free%", String.format ("%.2f", (double)root.getUsableSpace () / root.getTotalSpace ()));
			dmap.put ("readable", root.canRead ());
			dmap.put ("writable", root.canWrite ());
		}
		return map;
	}

	public Map<String, Object> mem () {
		Map<String, Object> map = new HashMap<> ();
		MemoryMXBean mxb = ManagementFactory.getMemoryMXBean ();
		map.put ("heap", mxb.getHeapMemoryUsage ());
		map.put ("non-heap", mxb.getNonHeapMemoryUsage ());
		map.put ("pending-finalization", mxb.getObjectPendingFinalizationCount ());
		map.put ("free", Runtime.getRuntime ().freeMemory ());
		map.put ("max", Runtime.getRuntime ().maxMemory ());
		map.put ("total", Runtime.getRuntime ().totalMemory ());
		return map;
	}

	public Map<String, Object> os () {
		Map<String, Object> map = new HashMap<> ();
		OperatingSystemMXBean mxb = ManagementFactory.getOperatingSystemMXBean ();
		map.put ("arch", mxb.getArch ());
		map.put ("processors", mxb.getAvailableProcessors ());
		map.put ("name", mxb.getName ());
		map.put ("load-avg", String.format ("%.2f", mxb.getSystemLoadAverage ()));
		map.put ("version", mxb.getVersion ());
		return map;
	}

	public Map<String, Object> compile () {
		Map<String, Object> map = new HashMap<> ();
		CompilationMXBean mxb = ManagementFactory.getCompilationMXBean ();
		if (mxb != null) {
			map.put ("compilation time", mxb.getTotalCompilationTime ());
			map.put ("name", mxb.getName ());
		}
		return map;
	}

	public Map<String, Object> top () {
		Map<String, Object> map = new HashMap<> ();
		ThreadMXBean mxb = ManagementFactory.getThreadMXBean ();
		ThreadInfo[] res = mxb.dumpAllThreads (true, true);
		long cputotal = 0;
		long usertotal = 0;
		Collection<Map<String, Object>> threads = new LinkedList<> ();
		map.put ("threads", threads);
		for (ThreadInfo thinfo:res) {
			Map<String, Object> infomap = new HashMap<> ();
			threads.add (infomap);
			infomap.put ("name", thinfo.getThreadName ());
			infomap.put ("id", thinfo.getThreadId ());
			infomap.put ("state", thinfo.getThreadState ());
			long cputime = mxb.getThreadCpuTime (thinfo.getThreadId ());
			infomap.put ("cpu", cputime);
			long usertime = mxb.getThreadUserTime (thinfo.getThreadId ());
			infomap.put ("user", usertime);
			infomap.put ("%", Math.round (((double)usertime / cputime * 10000)) / 100.);
			cputotal += cputime;
			usertotal += usertime;
		}
		map.put ("cpu", cputotal);
		map.put ("user", usertotal);
		map.put ("%", Math.round (((double)usertotal / cputotal * 10000)) / 100.);
		map.put ("execution_time", System.nanoTime () - startTime);
		map.put ("cpu%", Math.round ((double)cputotal / (System.nanoTime () - startTime) * 10000) / 100.);
		return map;
	}

	public long maxmem () {
		return Runtime.getRuntime ().maxMemory ();
	}

	public long totalmem () {
		return Runtime.getRuntime ().totalMemory ();
	}

	public void gc () {
		System.gc ();
	}
	//
	//	public void tracecalls (boolean on) {
	//		Runtime.getRuntime ().traceMethodCalls (on);
	//	}

	//
	// public String sh (String... _cmd) throws IOException {
	// String cmd = "";
	// for (String c:_cmd) {
	// if (cmd.length () > 0)
	// cmd += " ";
	// cmd += c;
	// }
	// return sh (cmd);
	// }
	//
	@MethodDescription ("Execute a shell command")
	@ParameterNames ("")
	public String sh (String cmd) throws IOException {
		Process process;
		String os = prop ("os.name");
		if (os == null) {
			return "Unknown OS";
		}

		os = os.toLowerCase ();
		if (os.startsWith ("win")) {
			process = Runtime.getRuntime ().exec ("cmd /c \"" + cmd + "\"");
		} else if (os.startsWith ("linux")) {
			process = Runtime.getRuntime ().exec (cmd);
		} else {
			return "Unsupported OS " + os;
		}

		InputStream is = process.getInputStream ();
		String output = "";
		int len;
		byte[] buff = new byte[1024];
		try {
			while ((len = is.read (buff)) > 0) {
				output += new String (buff, 0, len);
			}
		} catch (IOException e) {
		}
		is.close ();
		return output;
	}

	public ObjectList fs () {
		ObjectList list = new ObjectList ();
		for (File root:File.listRoots ()) {
			list.add (file (root.getAbsolutePath ()));
		}
		return list;
	}

	public ObjectMap file (String path) {
		File file = new File (path);
		ObjectMap map = new ObjectMap ();
		map.put ("path", file.getAbsolutePath ());
		map.put ("uri", file.toURI ());
		map.put ("directory", file.isDirectory ());

		if (file.isDirectory ()) {
			ObjectMap space = new ObjectMap ();
			map.put ("space", space);
			space.put ("free", file.getFreeSpace ());
			space.put ("total", file.getTotalSpace ());
			space.put ("usable", file.getUsableSpace ());
			if (file.getTotalSpace () > 0)
				space.put ("available", (Math.round ((double)file.getFreeSpace () / file.getTotalSpace () * 100)) + "%");
		} else {
			long lastmodified = file.lastModified ();
			ObjectMap lm = new ObjectMap ();
			map.put ("modified", lm);
			lm.put ("ts", lastmodified);
			if (lastmodified > 0) {
				lm.put ("date", new Date (lastmodified));
				lm.put ("msago", System.currentTimeMillis () - lastmodified);
			}
			map.put ("executable", file.canExecute ());
		}

		map.put ("readable", file.canRead ());
		map.put ("writable", file.canWrite ());
		map.put ("hidden", file.isHidden ());
		return map;
	}

	public ObjectMap all () {
		ObjectMap map = new ObjectMap ();
		map.put ("memory", mem ());
		map.put ("env", env ());
		map.put ("properties", props ());
		map.put ("processors", processors ());
		map.put ("os", os ());
		map.put ("top", top ());
		map.put ("compile", compile ());
		try {
			map.put ("mac", mac ());
		} catch (SocketException e) {
		}
		try {
			map.put ("network", network ());
		} catch (SocketException e) {
		}
		return map;
	}
}

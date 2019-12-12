package ca.redleafsolutions.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.redleafsolutions.Nagger;
import ca.redleafsolutions.NaggerVictim;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class Main implements NaggerVictim {
	private File root;
	private URL url;
	private String filename;
	private boolean onstart;
	private int interval;

	public Main(JSONItem json) {
		try {
			this.root = new File (json.getString ("dir"));
			this.url = new URL (json.getString ("url"));
			this.filename = new File (url.getPath ()).getName ();
			try {
				this.onstart = json.getBoolean ("check-start");
			} catch (JSONValidationException e) {
				this.onstart = true;
			}
			try {
				this.interval = json.getInt ("check-minutes");
			} catch (JSONValidationException e) {
				this.interval = 0;
			}
		} catch (JSONValidationException e) {
			iLogger.severe (e);
		} catch (IOException e) {
			iLogger.severe (e);
		}

		if (!done ()) {
			new Nagger (this, "Check for updates every " + interval + " minutes").start ();
		}
	}

	@MethodDescription ("Check if there's an update to current verison")
	public boolean check () throws IOException {
		iLogger.info ("Checking " + url + " for an upgrade");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection ();
		conn.setRequestMethod ("HEAD");
		if (conn.getResponseCode () == 200) {
			String contentlength = conn.getHeaderField ("Content-Length");
			if (contentlength != null) {
				try {
					// String date = conn.getHeaderField ("Date");
					// String lastmodified = conn.getHeaderField
					// ("Last-Modified");
					// Trace.info ("date: " + date + ", last mod: " +
					// lastmodified);

					int lenweb = Integer.parseInt (contentlength);
					File file = new File (root, filename);
					int lenfile = (int)file.length ();
					iLogger.info ("Upgrade file size: remote " + lenweb + ", local " + lenfile);
					// if length of file on the web is different from file on
					// file system
					if (lenweb != lenfile) {
						return true;
					}
				} catch (NumberFormatException e) {
				}
			}
		} else {
			iLogger.warning ("Return code: " + conn.getResponseCode ());
		}

		return false;
	}

	@MethodDescription ("Download upgrade version to local machine")
	public synchronized void download () throws IOException {
		iLogger.info ("Downloading an update file from " + url);
		File file = new File (root, filename);
		saveResource (url, file);
		iLogger.info ("Upgrade file was downloaded and saved at " + file.getAbsolutePath ());
	}

	@MethodDescription ("check if there's new updates and if so, dowload and restart")
	public void checkAndUpgrade () throws IOException {
		checkAndUpgrade (true);
	}

	@MethodDescription ("check if there's new updates and if so, dowload and restart (or not)")
	@ParameterNames ("restart")
	@ParameterDescriptions ("'true' to restart when complete update download")
	public synchronized void checkAndUpgrade (boolean restart) throws IOException {
		if (check ()) {
			download ();
			extract ();
			if (restart)
				restart ();
		}
	}

	private synchronized void extract () throws IOException {
		File zipfile = new File (root, filename);
		ZipInputStream zip = new ZipInputStream (new FileInputStream (zipfile));
		ZipEntry entry;
		while ((entry = zip.getNextEntry ()) != null) {
			// entry.getCompressedSize ());
			File file = new File (entry.getName ());
			if (entry.getSize () > 0) {
				if (file.exists ()) {
					iLogger.info ("Overwriting " + entry.getName ());
				} else {
					iLogger.info ("Adding " + entry.getName ());
				}
				File dir = file.getParentFile ();
				if ((dir != null) && !dir.exists ()) {
					dir.mkdirs ();
				}
				OutputStream os = new FileOutputStream (file);
				byte[] buffer = new byte[4096];
				int len;
				while ((len = zip.read (buffer)) > 0) {
					os.write (buffer, 0, len);
				}
				os.close ();
			} else {
				file.mkdirs ();
			}
		}
		zip.close ();
	}

	public void restart () {
		iLogger.info ("Upgrade shutting uplication down to take the update");
		System.exit (0);
	}

	private void saveResource (URL url, File file) throws IOException {
		saveResource (url, file, null, null);
	}

	private void saveResource (URL url, File file, String prefix, String suffix) throws IOException {
		iLogger.info ("Calling " + url.toString ());
		URLConnection conn = url.openConnection ();
		InputStream is = conn.getInputStream ();
		byte[] buff = new byte[4096];
		if ((file.getParentFile () != null) && !file.getParentFile ().exists ()) {
			file.getParentFile ().mkdirs ();
		}
		OutputStream os = new FileOutputStream (file);
		if (prefix != null)
			os.write (prefix.getBytes ());
		int len, total = 0;
		while ((len = is.read (buff)) > 0) {
			os.write (buff, 0, len);
			total += len;
		}
		is.close ();
		if (suffix != null)
			os.write (suffix.getBytes ());
		os.close ();
		iLogger.info ("Save file " + file.getName () + " total of " + total + " bytes");
	}

	//
	// private void saveResource (String content, File file, String prefix,
	// String suffix) throws IOException {
	// if ((file.getParentFile () != null) && !file.getParentFile ().exists ())
	// {
	// file.getParentFile ().mkdirs ();
	// }
	// OutputStream os = new FileOutputStream (file);
	// if (prefix != null)
	// os.write (prefix.getBytes ());
	// os.write (content.getBytes ());
	// if (suffix != null)
	// os.write (suffix.getBytes ());
	// os.close ();
	// }
	//
	// private JSONObject loadJSON (URL url) throws IOException, JSONException {
	// iLogger.info ("Calling " + url.toString ());
	// long tick = System.currentTimeMillis ();
	// URLConnection conn = url.openConnection ();
	// InputStream is = conn.getInputStream ();
	// byte[] buff = new byte[4096];
	// int len;
	// String s = new String ();
	// while ((len = is.read (buff)) > 0) {
	// s += new String (buff, 0, len);
	// }
	// is.close ();
	// long tock = System.currentTimeMillis ();
	// iLogger.info ("Load " + url + " complete. Size " + s.length () +
	// " bytes, duration " + (tock - tick) / 1000 + " sec, speed "
	// + ((tock - tick) / s.length () * 1.024) + " Kbps");
	// return new JSONObject (s);
	// }

	@Override
	@IShellInvisible
	public void again () {
		try {
			checkAndUpgrade (false);
		} catch (IOException e) {
			handleNaggerException (e);
		}
	}

	@Override
	@IShellInvisible
	public void waitCondition () {
		if (onstart) {
			onstart = false;
		} else {
			try {
				Thread.sleep (interval * 60 * 1000);
			} catch (InterruptedException e) {
				handleNaggerException (e);
			}
		}
	}

	@Override
	@IShellInvisible
	public boolean done () {
		return !onstart && (interval <= 0);
	}

	@Override
	@IShellInvisible
	public void handleNaggerException (Throwable e) {
		iLogger.severe (e);
	}
}

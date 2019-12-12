package ca.redleafsolutions.ishell2.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.ishell2.IShellObject;
import ca.redleafsolutions.ishell2.IShellRequestScript;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.ishell2.engines.IShellEngine;
import ca.redleafsolutions.ishell2.engines.ScriptableIShellEngine;

public class Script {
	private IShellEngine engine;
	private File root;

	public Script (IShellEngine engine) {
		this.engine = engine;
		if (engine instanceof ScriptableIShellEngine) {
			this.root = ((ScriptableIShellEngine)engine).getScriptsRoot ();
		} else {
			this.root = new File ("").getAbsoluteFile ();
		}
	}

	@MethodDescription ("Execute script")
	@ParameterNames ("filename")
	@ParameterDescriptions ("Name of script file")
	public IShellObject run (String fname) throws IOException {
		return run (new File (root, fname));
	}
	@MethodDescription ("Execute script")
	@ParameterNames ({ "filename", "extension" })
	@ParameterDescriptions ({ "Name of script file", "File extension" })
	public IShellObject run (String fname, String fext) throws IOException {
		return run (new File (root, fname + "." + fext));
	}
	private IShellObject run (File file) throws IOException {
		IShellRequestScript request = new IShellRequestScript (new String (Files.readAllBytes (file.toPath ())));
		IShellObject res = engine.execute (request);
		if (res instanceof IShellObject.ExceptionObject) {
			throw new IOException ((Exception)res.getObject ());
		}
		return res;
	}
	
	@MethodDescription ("Change the script current directory")
	@ParameterNames ("path")
	@ParameterDescriptions ("Relative path to change the directory to")
	public void cd (String path) {
		root = new File (root, path);
	}
	
	@MethodDescription ("Get script current directory")
	public String pwd () {
		return root.getAbsolutePath ();
	}
	
	@MethodDescription ("Get script current directory")
	public List<File> list () {
		return Arrays.asList (root.listFiles ());
	}
	
	@MethodDescription ("Get script root object")
	public File root () {
		return root;
	}
	
	@MethodDescription ("Get script content")
	@ParameterNames ("filename")
	@ParameterDescriptions ("Name of script file")
	public String content (String fname) throws IOException {
		return content (new File (root, fname));
	}
	@MethodDescription ("Get script content")
	@ParameterNames ({ "filename", "extension" })
	@ParameterDescriptions ({ "Name of script file", "File extension" })
	public String content (String fname, String fext) throws IOException {
		return content (new File (root, fname + "." + fext));
	}
	private String content (File file) throws IOException {
		InputStream is = new FileInputStream (file);
		try {
			return new String (IOUtils.readFully (is, (int)file.length ()));
		} finally {
			is.close ();
		}
	}
	
	@MethodDescription ("Save script")
	@ParameterNames ({ "filename", "content" })
	@ParameterDescriptions ({ "Name of script file", "File content" })
	public ObjectMap save (String fname, String content) throws IOException {
		return save (new File (root, fname), content);
	}
	private ObjectMap save (File file, String content) throws IOException {
		ObjectMap map = new ObjectMap ();
		long tic = System.nanoTime ();
		FileOutputStream os = new FileOutputStream (file);
		try {
			os.write (content.getBytes ());
			map.put ("name", file.getName ());
			map.put ("length", content.length ());
		} finally {
			os.close ();
		}
		map.put ("duration", (System.nanoTime () - tic)/1000000.);
		return map;
	}

	
	@MethodDescription ("Delete file or directory")
	@ParameterNames ("filename")
	@ParameterDescriptions ("Name of file")
	public ObjectMap delete (String fname) throws IOException {
		return delete (new File (root, fname));
	}
	@MethodDescription ("Delete file or directory")
	@ParameterNames ({ "filename", "extension" })
	@ParameterDescriptions ({ "Name of script file", "File extension" })
	public ObjectMap delete (String fname, String fext) throws IOException {
		return delete (new File (root, fname + "." + fext));
	}
	private ObjectMap delete (File file) {
		ObjectMap map = new ObjectMap ();
		long tic = System.nanoTime ();
		file.delete ();
		map.put ("duration", (System.nanoTime () - tic)/1000000.);
		return map;
	}

	public String controlpanel () throws IOException {
		ResourceLocator locator = new ResourceLocator (Script.class);
		InputStream is = locator.getInputStream ("scriptcp.html");
		if (is == null) {
			throw new FileNotFoundException ("scriptcp.html");
		}
		ObjectMap map = new ObjectMap ();
		map.put ("ishell", iShell.getInstance ());
		map.put ("scripts", list ());
		try {
			return TemplateUtils.evaluate (is, map);
		} finally {
			if (is != null)
				try {
					is.close ();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

}

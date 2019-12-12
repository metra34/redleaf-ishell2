package ca.redleafsolutions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JARUtils {
	public static void loadJAR (String path) throws IOException {
		File file = new File(path);
		loadJAR(file);
	}
	
	public static void loadJAR(File file) throws IOException {
	       URL url = file.toURI().toURL();
	       URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	        
	       try {
	        	Class<?>[] parameters = new Class[] {URL.class};
	            Method method = URLClassLoader.class.getDeclaredMethod("addURL", parameters);
	            method.setAccessible(true);
	            method.invoke(sysloader, new Object[] {url});
	        } catch (Throwable t) {
	            throw new IOException("Error, could not add URL to system classloader");
	        }
		}
}

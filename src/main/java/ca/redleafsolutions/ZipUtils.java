package ca.redleafsolutions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;

public class ZipUtils {
	
	public ZipUtils () {
		
	}
	public static String zip (String location) throws IOException {
		return zip(new File(location)).getAbsolutePath();
	}

	public static void unzip (String in, String out) throws IOException {
		unzip(new File(in), new File(out));
	}

	@IShellInvisible
	public static File zip (File file) throws IOException {
		if (file.isFile())  {
			return zipfile(file);
		} else {
			return zipfolder(file);
		}
	}
	@IShellInvisible
	public static void unzip (File file, File folder) throws IOException {
		try{
			if(!folder.exists()){
				folder.mkdir();
	    	}
			
			if (!folder.isDirectory()) {
				throw new IOException("Output " + folder.getName() + " is not a directory");
			}
			 
			byte[] buffer = new byte[1024];
			
	    	//get the zip file content
	    	ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
	    	//get the zipped file list entry
	    	ZipEntry ze = zis.getNextEntry();
	 
	    	while(ze!=null){
	 
	    	   String fileName = ze.getName();
	           File newFile = new File(folder + File.separator + fileName);
	 
	           //System.out.println("file unzip : "+ newFile.getAbsoluteFile());
	 
	            //create all non exists folders
	            //else you will hit FileNotFoundException for compressed folder
	            new File(newFile.getParent()).mkdirs();
	 
	            FileOutputStream fos = new FileOutputStream(newFile);             
	 
	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	       		fos.write(buffer, 0, len);
	            }
	 
	            fos.close();   
	            ze = zis.getNextEntry();
	    	}
	 
	        zis.closeEntry();
	    	zis.close();
	    }catch(IOException ex){
	       ex.printStackTrace(); 
	    }
	}
	
	private static File zipfolder(File file1) throws IOException {
		File tempfile = null;
		InputStream is = null;
		ZipOutputStream zip = null;
		try {
			tempfile = File.createTempFile ("tmp.", ".zip");
			zip = new ZipOutputStream (new BufferedOutputStream (new FileOutputStream (tempfile)));

			for(File file : file1.listFiles()) {
				zip.putNextEntry (new ZipEntry (file.getName ()));
				is = new FileInputStream (file);
				byte[] buffer = new byte[1024];
				int count;
				while ((count = is.read (buffer)) != -1) {
					zip.write (buffer, 0, count);
				}
			}

			return tempfile;
		} catch (IOException e) {
			try {
				tempfile.delete ();
			} catch (Throwable e2) {
			}
			throw e;
		} finally {
			try {
				is.close ();
			} catch (Throwable e2) {
			}
			try {
				zip.close ();
			} catch (Throwable e2) {
			}
		}
	}

	private static File zipfile (File file) throws IOException {
		File tempfile = null;
		InputStream is = null;
		ZipOutputStream zip = null;
		try {
			tempfile = File.createTempFile ("tmp.", ".zip");
			zip = new ZipOutputStream (new BufferedOutputStream (new FileOutputStream (tempfile)));
			
			zip.putNextEntry (new ZipEntry (file.getName ()));
			is = new FileInputStream (file);
			byte[] buffer = new byte[1024];
			int count;
			while ((count = is.read (buffer)) != -1) {
				zip.write (buffer, 0, count);
			}

			return tempfile;
		} catch (IOException e) {
			try {
				tempfile.delete ();
			} catch (Throwable e2) {
			}
			throw e;
		} finally {
			try {
				is.close ();
			} catch (Throwable e2) {
			}
			try {
				zip.close ();
			} catch (Throwable e2) {
			}
		}
	}
}

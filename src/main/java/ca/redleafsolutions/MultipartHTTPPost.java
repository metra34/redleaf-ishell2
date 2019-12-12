package ca.redleafsolutions;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 * @author www.codejava.net
 *
 */
public class MultipartHTTPPost {
    private CloseableHttpClient client;
	private MultipartEntityBuilder builder;
	private String url;
    
	public MultipartHTTPPost(String _url) {
		url = _url;
		client =  HttpClientBuilder.create().build();
		builder = MultipartEntityBuilder.create();        
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
   	}
 
    /**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
     */
    public void addField(String name, String value) {
    	builder.addTextBody(name, value);
    }
 
    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in &lt;input type="file" name="..." /&gt;
     * @param uploadFile a File to be uploaded
     */
    public void addFile(String fieldName, File uploadFile) {
    	builder.addPart(fieldName, new FileBody(uploadFile));
    }
    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException if client fails to open HTTP connection
     */
    public String execute() throws IOException {
		HttpPost post = new HttpPost(url);
		post.setEntity(builder.build());
		HttpResponse response;
		response = client.execute(post);
		String s = EntityUtils.toString(response.getEntity());
		return s;
    }
}
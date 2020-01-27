package ca.redleafsolutions.ishell2.interfaces.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;

import ca.redleafsolutions.ishell2.StringOutputStream;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class MimeHandler {
	// private String boundary;
	private Map<String, Object> params;
	private String boundary;
	private Object body;
	private String contentType = "";

	public MimeHandler() {
	}

	public boolean addHeaderEntry(String key, String value) {
		if (key.equalsIgnoreCase("content-type")) {
			contentType = value.toLowerCase();
			if (isMultiPart()) {
				String[] lst = value.split(";");
				if (lst.length > 1) {
					boundary = lst[1].trim();
					lst = boundary.split("=");
					if ("boundary".equals(lst[0].trim())) {
						boundary = lst[1].trim();
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isMultiPart() {
		return contentType.startsWith("multipart/");
	}
	public boolean isText() {
		return contentType.startsWith("text/");
	}
	public boolean isJSON() {
		return contentType.endsWith("/json");
	}

	public Object getBody () {
		return body;
	}

	public Map<String, Object> parseRequest(InputStream is) throws IOException {
		if (isJSON ()) {
			try {
				body = JSONItem.parse(getBody(is));
			} catch (JSONValidationException e) {
				throw new IOException(e);
			}
		} else if (isText ()) {
			body = getBody(is);
		} else if (boundary != null) {
			return parseMultipartRequest(is);
		} else {
			return parsePostRequest(is);
		}
		return params = new HashMap<String, Object> ();
	}


	public Map<String, Object> parseMultipartRequest(InputStream is) throws IOException {
		params = new TreeMap<String, Object>();
		try {
			MultipartStream multipartStream = new MultipartStream(is, boundary.getBytes(), 1024, null);
			boolean nextPart = multipartStream.skipPreamble();
			while (nextPart) {
				String header = multipartStream.readHeaders();
				Map<String, String> headers = readHeaders(header);
				String filename = headers.get("filename");

				OutputStream output;
				File file = null;
				if (filename != null) {
					file = File.createTempFile(filename + ".", null);
					output = new FileOutputStream(file);
				} else {
					output = new StringOutputStream();
				}
				multipartStream.readBodyData(output);

				params.put(headers.get("name"), (file != null) ? file : output.toString());

				nextPart = multipartStream.readBoundary();
				output.close();
			}
		} catch (MultipartStream.MalformedStreamException e) {
			// the stream failed to follow required syntax
			throw new IOException(e);
		}
		return params;
	}

	public String getBody(InputStream is) throws IOException {
		StringOutputStream os = new StringOutputStream();
		IOUtils.copy(is, os);
		os.close();
		body = os.toString();
		return body.toString();
	}

	public Map<String, Object> parsePostRequest(InputStream is) throws IOException {
		params = new TreeMap<String, Object>();
		String[] pairs = getBody(is).toString().split("&");
		for (String pair : pairs) {
			String[] kv = pair.split("=");
			if (kv.length > 1) {
				String key = kv[0];
				String value = kv[1];
				String urlDecodedValue = URLDecoder.decode(value, CharEncoding.UTF_8);
				params.put(key, urlDecodedValue != null ? urlDecodedValue : value);
			}
		}
		return params;
	}

	private Map<String, String> readHeaders(String header) throws IOException {
		Map<String, String> map = new TreeMap<String, String>();
		for (String line : header.split(System.lineSeparator())) {
			if (line != null) {
				line = line.trim();

				String[] lst = line.split(":");
				if (lst.length > 1) {
					String key = lst[0].trim().toLowerCase();
					// String value = StringUtils.join (Arrays.copyOfRange
					// (lst, 1, lst.length - 1), ":");
					String value = lst[1].trim();
					String[] lst2 = value.split(";");
					value = lst2[0].trim();
					map.put(key, value);
					for (int i = 1; i < lst2.length; ++i) {
						String[] lst3 = lst2[i].split("=");
						if (lst3.length > 1)
							map.put(lst3[0].trim().toLowerCase(), StringUtils.strip(lst3[1], "\" \t\r\n"));
					}
				}
			}
		}
		return map;
	}

	public Map<String, Object> getParams() {
		return params;
	}
}
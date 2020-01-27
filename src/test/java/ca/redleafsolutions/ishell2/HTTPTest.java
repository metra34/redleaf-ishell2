package ca.redleafsolutions.ishell2;

import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONUtils;
import ca.redleafsolutions.json.JSONValidationException;

public class HTTPTest {
	private iShell ishell;

	private void init() {
		ishell = iShell.getInstance();
		if (ishell == null) {
			iShell.main(new String[] {});
			ishell = iShell.getInstance();
		}
	}

	@Test
	public void JSONBody() throws JSONValidationException, IOException {
		init();

		JSONItem json = JSONItem.newObject().put("int", 123).put("str", "ABC").put("obj", new Date().toString())
				.put("list", JSONItem.newArray().put("another string").put(555));

		String serverurl = ishell.http().getServerURL();
		URL url = new URL(serverurl + "http/callback/jsonbody.json");
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			String body = json.toString();

			connection.setRequestProperty("Content-Length", Integer.toString(body.length()));
			connection.setRequestProperty("Content-Type", "application/json");
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(body.getBytes());
			wr.flush();

			int code = connection.getResponseCode();
			assertTrue((code >= 200) && (code < 300));
			int contentlen = connection.getContentLength();
			InputStream is = connection.getInputStream();
			StringBuffer response = new StringBuffer();
			response.append(new String(IOUtils.readFully(is, contentlen), "UTF-8"));
			JSONItem responsejson = JSONItem.parse(response.toString());
			assertTrue(JSONUtils.diff(json, responsejson).length() == 0);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	@Test
	public void JSONParam() throws JSONValidationException, IOException {
		init();

		JSONItem json = JSONItem.newObject().put("int", 123).put("str", "ABC").put("obj", new Date().toString())
				.put("list", JSONItem.newArray().put("another string").put(555));

		String serverurl = ishell.http().getServerURL();
		URL url = new URL(serverurl + "http/callback/jsonparam.json");
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			String paramstr = "json=" + URLEncoder.encode(json.toString(), "UTF-8");

			connection.setRequestProperty("Content-Length", Integer.toString(paramstr.length()));
			// connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Type", "multipart/form-data");
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(paramstr.getBytes());
			wr.flush();

			int code = connection.getResponseCode();
			assertTrue((code >= 200) && (code < 300));
			int contentlen = connection.getContentLength();
			InputStream is = connection.getInputStream();
			StringBuffer response = new StringBuffer();
			response.append(new String(IOUtils.readFully(is, contentlen), "UTF-8"));
			JSONItem responsejson = JSONItem.parse(response.toString());
			assertTrue(JSONUtils.diff(json, responsejson).length() == 0);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	@Test
	public void headerTest() throws JSONValidationException, IOException {
		init();

		String serverurl = ishell.http().getServerURL();
		URL url = new URL(serverurl + "http/callback/headers.json");
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			String random = UUID.randomUUID().toString();
			connection.addRequestProperty("Ishell-header", random);

			int code = connection.getResponseCode();
			assertTrue((code >= 200) && (code < 300));
			int contentlen = connection.getContentLength();
			InputStream is = connection.getInputStream();
			StringBuffer response = new StringBuffer();
			response.append(new String(IOUtils.readFully(is, contentlen), "UTF-8"));
			JSONItem responsejson = JSONItem.parse(response.toString());
			assertTrue(random.equals(responsejson.getString("Ishell-header")));
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	@ParameterNames("json")
	public IShellRequestConsumer callback() {
		return new HTTPCallback();
	}

	public static class HTTPCallback implements IShellRequestConsumer {
		private IShellRequestSingle request;

		@Override
		@IShellInvisible
		public void setRequest(IShellRequestSingle request) {
			this.request = request;
		}

		public Object jsonbody() {
			if (this.request instanceof IShellRequestHTTP) {
				return ((IShellRequestHTTP) this.request).getBody();
			}
			return null;
		}

		@ParameterNames ("json")
		public JSONItem jsonparam(JSONItem json) {
			return json;
		}

		public ObjectMap headers() {
			if (this.request instanceof IShellRequestHTTP) {
				return ((IShellRequestHTTP) this.request).getRequestHeaders();
			}
			return null;
		}
	};
}

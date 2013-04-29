package at.gv.egiz.android.communication.http;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import at.gv.egiz.android.signature.exception.EgizHttpCommunicatorException;

/**
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class HttpCommunicator {

	private DefaultHttpClient httpClient = null;

	public HttpCommunicator(DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * 
	 * @param url
	 * @param parameters
	 * @return the http response
	 * @throws EgizHttpCommunicatorException
	 */
	public String post(String url, List<NameValuePair> parameters)
			throws EgizHttpCommunicatorException {
		if (httpClient != null) {
			try {

				HttpPost httpPost = new HttpPost(url);

				if (parameters != null)
					httpPost.setEntity(new UrlEncodedFormEntity(parameters));

				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				}

			} catch (ParseException e) {
				e.printStackTrace();
				throw new EgizHttpCommunicatorException(
						"Exception occured while processing response. Please retry and send post again.");

			} catch (IOException e) {
				e.printStackTrace();
				throw new EgizHttpCommunicatorException(
						"Exception occurred while executing http post. Please retry and send post again.");

			}
		}
		return null;
	}
}

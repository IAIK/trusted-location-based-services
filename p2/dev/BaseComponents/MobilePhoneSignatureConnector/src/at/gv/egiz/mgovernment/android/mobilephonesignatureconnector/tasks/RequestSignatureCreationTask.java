package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities.HandySignaturActivity;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.ATrustConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.Constants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.EgizHttpCommunicatorException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.NoInternetConnectionAvailableException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.PasswordMissingOrTooShortException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.SignatureCreationFailedException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongTanException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongUserCredentialsException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.ConnectionUtils;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.HttpCommunicator;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.SessionData;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.SignatureUtils;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */
public class RequestSignatureCreationTask extends
		AsyncTask<String, Void, SessionData> {

	// if an error occured this Exception will be passed to the calling activity
	// to provide details about the error
	private Exception e = null;

	private HandySignaturActivity activity = null;
	private Boolean useTestSignature = false;

	private HttpCommunicator httpCommunicator = null;

	private static final String ERROR_WRONG_NUMBER_OF_PARAMETERS = "Wrong number of parmeters - only one parameter expected.";
	private static final String REQUEST_PART_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<sl:CreateXMLSignatureRequest xmlns:sl=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\">"
				+ "<sl:KeyboxIdentifier>SecureSignatureKeypair</sl:KeyboxIdentifier>"
				+ "<sl:DataObjectInfo Structure=\"enveloping\">"
				+ "<sl:DataObject>" + "<sl:XMLContent>";
	private static final String REQUEST_PART_2 = "</sl:XMLContent>" + "</sl:DataObject>"
				+ "<sl:TransformsInfo>" + "<sl:FinalDataMetaInfo><sl:MimeType>text/plain</sl:MimeType></sl:FinalDataMetaInfo>"
				+ "</sl:TransformsInfo>" + "</sl:DataObjectInfo>"
				+ "</sl:CreateXMLSignatureRequest>";
	
	
	public RequestSignatureCreationTask(HttpCommunicator httpCommunicator,
			HandySignaturActivity activity, Boolean useTestSignature) {

		this.httpCommunicator = httpCommunicator;
		this.activity = activity;
		this.useTestSignature = useTestSignature;
	}

	@Override
	/**
	 * params[0] : content that should be signed
	 * params[1] : TransformsInfo mimeType
	 */
	protected SessionData doInBackground(String... params) {

		if (!ConnectionUtils.isInternetConnectionAvailable(activity
				.getApplicationContext())) {
			this.e = new NoInternetConnectionAvailableException(
					Constants.NO_INTERNET_CONNECTION_AVAILABLE);
			return null;
		}

		if (params == null || params.length != 1) {
			this.e = new SignatureCreationFailedException(
					ERROR_WRONG_NUMBER_OF_PARAMETERS);
			return null;
		}

		SessionData result = null;
		String requestString = REQUEST_PART_1 + params[0] + REQUEST_PART_2;
		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair(ATrustConstants.paramXMLRequest, requestString));

		try {
			String url = useTestSignature ? ATrustConstants.requestTestUrl
					: ATrustConstants.requestUrl;

			String response = httpCommunicator.post(url, pair);

			// check if error has occurred
			try {
				SignatureUtils.checkForErrorResponse(response);
			} catch (WrongTanException e1) {
				this.e = e1;
			} catch (SignatureCreationFailedException e1) {
				this.e = e1;
			} catch (WrongUserCredentialsException e1) {
				this.e = e1;
			} catch (PasswordMissingOrTooShortException e1) {
				this.e = e1;
			}
			// extract data like event validation, etc. from the response
			result = SignatureUtils.extractSessionData(response);
		} catch (EgizHttpCommunicatorException e) {
			this.e = e;
		}

		return result;

	}

	@Override
	public void onPostExecute(SessionData sessionData) {

		if (e != null) {
			activity.handleError(e);
		}
	}

}

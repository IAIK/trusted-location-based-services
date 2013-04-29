package at.gv.egiz.android.signature.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import at.gv.egiz.android.R;
import at.gv.egiz.android.communication.http.HttpCommunicator;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.signature.ATrust;
import at.gv.egiz.android.signature.Error;
import at.gv.egiz.android.signature.SessionData;
import at.gv.egiz.android.signature.SignatureUtils;
import at.gv.egiz.android.signature.activities.HandySignaturActivity;
import at.gv.egiz.android.signature.exception.EgizHttpCommunicatorException;
import at.gv.egiz.android.signature.exception.NoInternetConnectionAvailableException;
import at.gv.egiz.android.signature.exception.PasswordMissingOrTooShortException;
import at.gv.egiz.android.signature.exception.SignatureCreationFailedException;
import at.gv.egiz.android.signature.exception.WrongTanException;
import at.gv.egiz.android.signature.exception.WrongUserCredentialsException;
import at.gv.egiz.android.util.ConnectionUtils;

/**
 * Responsible for sending the initial request for signature creation with the
 * Mobile BKU. Sending the request is realized by using AsyncTask class,
 * therefore the UI does not get blocked during waiting for the response.
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class RequestSignatureCreationTask extends
		AsyncTask<String, Void, SessionData> {

	// if an error occured this Exception will be passed to the calling activity
	// to provide details about the error
	private Exception e = null;

	private HandySignaturActivity activity = null;
	private Boolean useTestSignature = false;

	private String tag = DebugTags.EGIZ + " " + DebugTags.SIGNATURE
			+ " RequestSignatureCreationTask";

	private HttpCommunicator httpCommunicator = null;
	private ProgressDialog dialog;

	public RequestSignatureCreationTask(HttpCommunicator httpCommunicator,
			HandySignaturActivity activity, Boolean useTestSignature) {
		this.httpCommunicator = httpCommunicator;
		this.activity = activity;
		this.useTestSignature = useTestSignature;
	}

	@Override
	protected void onPreExecute() {
		// show progress dialog
		dialog = new ProgressDialog(this.activity);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(activity
				.getString(R.string.signature_progress_requestsignaturecreation));
		dialog.show();
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
					Error.NO_INTERNET_CONNECTION_AVAILABLE);
			return null;
		}

		if (params == null || params.length != 2) {
			this.e = new SignatureCreationFailedException(
					"Not enough parameter! Usage: First parameter is the content to sign and second parameter is the mime type in form of eg. application/xml !");
			return null;
		}

		SessionData result = null;
		String requestString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<sl:CreateXMLSignatureRequest xmlns:sl=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\">"
				+ "<sl:KeyboxIdentifier>SecureSignatureKeypair</sl:KeyboxIdentifier>"
				+ "<sl:DataObjectInfo Structure=\"enveloping\">"
				+ "<sl:DataObject>" + "<sl:XMLContent>" + params[0]
				+ "</sl:XMLContent>" + "</sl:DataObject>"
				+ "<sl:TransformsInfo>" + "<sl:FinalDataMetaInfo><sl:MimeType>"
				+ params[1] + "</sl:MimeType></sl:FinalDataMetaInfo>"
				+ "</sl:TransformsInfo>" + "</sl:DataObjectInfo>"
				+ "</sl:CreateXMLSignatureRequest>";
		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair(ATrust.paramXMLRequest, requestString));
		try {
			String url = useTestSignature ? ATrust.requestTestUrl
					: ATrust.requestUrl;
			Log.i(tag, "Sending request to: " + url);
			Log.i(tag, pair.toString());
			String response = httpCommunicator.post(url, pair);
			Log.i(tag, "Response to initial request:");
			Log.i(tag, response);
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
			// extract data like eventvalidation,... from the response
			result = SignatureUtils.extractSessionData(response);
		} catch (EgizHttpCommunicatorException e) {
			this.e = e;
		}

		return result;

	}

	@Override
	public void onPostExecute(SessionData sessionData) {
		Log.d(tag, "onPostExecute()");
		dialog.dismiss();
		if (e != null) {
			activity.handleError(e);
		}

	}

}

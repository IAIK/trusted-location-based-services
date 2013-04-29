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
import at.gv.egiz.android.signature.SessionData;
import at.gv.egiz.android.signature.SignatureUtils;
import at.gv.egiz.android.signature.activities.HandySignaturActivity;
import at.gv.egiz.android.signature.exception.EgizHttpCommunicatorException;
import at.gv.egiz.android.signature.exception.PasswordMissingOrTooShortException;
import at.gv.egiz.android.signature.exception.SignatureCreationFailedException;
import at.gv.egiz.android.signature.exception.WrongTanException;
import at.gv.egiz.android.signature.exception.WrongUserCredentialsException;

/**
 * Sends telephone number and signature password to MobileBKU.
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class PostNumberAndPasswordTask extends
		AsyncTask<SessionData, Void, SessionData> {

	private Exception e = null;
	private HandySignaturActivity activity = null;

	private Boolean useTestSignature = false;

	private String tag = DebugTags.EGIZ + " " + DebugTags.SIGNATURE
			+ " PostNumberAndPasswordTask";

	private HttpCommunicator httpCommunicator = null;
	private ProgressDialog dialog;

	public PostNumberAndPasswordTask(HttpCommunicator httpCommunicator,
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
				.getString(R.string.signature_progress_postnumberandpassword));
		dialog.show();
	}

	@Override
	/**
	 * params[0] : the current SessionData object
	 */
	protected SessionData doInBackground(SessionData... params) {

		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair(ATrust.paramViewstate, params[0]
				.getViewstate()));
		pair.add(new BasicNameValuePair(ATrust.paramEventValidation, params[0]
				.getEventvalidation()));
		pair.add(new BasicNameValuePair(ATrust.paramPrefix, params[0]
				.getPrefix()));
		pair.add(new BasicNameValuePair(ATrust.paramTelNumber, params[0]
				.getMobilePhoneNumber()));
		pair.add(new BasicNameValuePair(ATrust.paramSignPassword, params[0]
				.getSignaturePassword()));
		pair.add(new BasicNameValuePair(ATrust.paramBtnIdentification,
				params[0].getButtonIdentification()));
		try {
			String url = useTestSignature ? ATrust.requestTestUrl
					: ATrust.requestUrl;
			Log.i(tag, "Sending user credentials to: " + url
					+ ATrust.identificationExtension + params[0].getSessionID());
			Log.i(tag, pair.toString());
			String response = httpCommunicator.post(
					url + ATrust.identificationExtension
							+ params[0].getSessionID(), pair);
			Log.i(tag, "Response to user credentials:");
			Log.i(tag, response);
			try {
				SessionData tempSessionData = SignatureUtils
						.extractSessionData(response);
				params[0].setViewstate(tempSessionData.getViewstate());
				params[0].setEventvalidation(tempSessionData
						.getEventvalidation());

				params[0].setReferenceValue(SignatureUtils
						.extractReferenceValue(response));
			} catch (SignatureCreationFailedException e) {
				this.e = e;
			}
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

		} catch (EgizHttpCommunicatorException e) {
			this.e = e;
		}
		return params[0];
	}

	@Override
	public void onPostExecute(SessionData sessionData) {
		super.onPostExecute(sessionData);
		dialog.dismiss();
		if (e != null) {
			Log.e(tag,
					"Exception while posting telephone number and signature password.");
			activity.handleError(e);
		}

	}

}

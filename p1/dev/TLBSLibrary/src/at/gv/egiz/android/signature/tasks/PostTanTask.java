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
import at.gv.egiz.android.util.XMLUtils;

/**
 * Sends the message with the tan to the Mobile BKU.
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class PostTanTask extends AsyncTask<SessionData, Void, SessionData> {

	private Exception e = null;
	private HandySignaturActivity activity = null;
	private Boolean useTestSignature = false;
	private String tag = DebugTags.EGIZ + " " + DebugTags.SIGNATURE
			+ " PostTanTask";

	private HttpCommunicator httpCommunicator = null;
	private ProgressDialog dialog;

	public PostTanTask(HttpCommunicator httpCommunicator,
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
				.getString(R.string.signature_progress_posttan));
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
		pair.add(new BasicNameValuePair(ATrust.paramInputTan, params[0]
				.getTan()));
		pair.add(new BasicNameValuePair(ATrust.paramBtnSign, params[0]
				.getSignButton()));

		try {

			String url = useTestSignature ? ATrust.requestTestUrl
					: ATrust.requestUrl;
			url += ATrust.signatureExtension + params[0].getSessionID();
			Log.i(tag, "Sending entered tan to:" + url);
			Log.i(tag, pair.toString());
			String response = httpCommunicator.post(url, pair);
			Log.i(tag, "Response: ");
			Log.i(tag, response);

			try {
				SignatureUtils.checkForErrorResponse(response);
				params[0].setSignature(XMLUtils
						.extractSignatureFromResponse(response));
			} catch (WrongTanException e1) {
				this.e = e1;
				int triesLeft = SignatureUtils.extractNumberOfTriesLeft(e1
						.getMessage());
				Log.i(tag, triesLeft + " tries left.");
				if (triesLeft > 0) {
					// Update Viewstate and eventvalidation
					params[0].setEventvalidation(SignatureUtils
							.extractSessionData(response).getEventvalidation());
					params[0].setViewstate(SignatureUtils.extractSessionData(
							response).getViewstate());
					params[0].setSignature(null);
					Log.i(tag, "TAN was false, send back button.");
					postBackButton(params[0]);
				}
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
			Log.e(tag, "Exception while posting TAN.");
			activity.handleError(e);
		}

	}

	private void postBackButton(SessionData sessionData) {

		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair(ATrust.paramViewstate, sessionData
				.getViewstate()));
		pair.add(new BasicNameValuePair(ATrust.paramEventValidation,
				sessionData.getEventvalidation()));
		pair.add(new BasicNameValuePair(ATrust.paramBtnBack, sessionData
				.getBackButton()));

		try {

			String url = useTestSignature ? ATrust.requestTestUrl
					: ATrust.requestUrl;

			url += ATrust.errorExtrension + sessionData.getSessionID()
					+ "&ls=2&errorcode=4";

			String response = httpCommunicator.post(url, pair);

			sessionData.setEventvalidation(SignatureUtils.extractSessionData(
					response).getEventvalidation());
			sessionData.setViewstate(SignatureUtils
					.extractSessionData(response).getViewstate());

			try {
				SignatureUtils.checkForErrorResponse(response);
			} catch (WrongTanException e1) {
				this.e = e1;
				return;
			} catch (SignatureCreationFailedException e1) {
				this.e = e1;
				return;
			} catch (WrongUserCredentialsException e1) {
				this.e = e1;
				return;
			} catch (PasswordMissingOrTooShortException e1) {
				this.e = e1;
				return;
			}

		} catch (EgizHttpCommunicatorException e) {
			this.e = e;
		}
		return;
	}
}

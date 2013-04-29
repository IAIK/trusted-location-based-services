package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities.HandySignaturActivity;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.ATrustConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.EgizHttpCommunicatorException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.PasswordMissingOrTooShortException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.SignatureCreationFailedException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongTanException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongUserCredentialsException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.HttpCommunicator;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.SessionData;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.SignatureUtils;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */
public class PostTanTask extends AsyncTask<SessionData, Void, SessionData> {

	private Exception e = null;
	private HandySignaturActivity activity = null;
	private Boolean useTestSignature = false;

	private HttpCommunicator httpCommunicator = null;

	private static final String URL_PARAMETERS = "&ls=2&errorcode=4"; 
	
	public PostTanTask(HttpCommunicator httpCommunicator,
			HandySignaturActivity activity, Boolean useTestSignature) {

		this.httpCommunicator = httpCommunicator;
		this.activity = activity;
		this.useTestSignature = useTestSignature;
	}

	@Override
	/**
	 * params[0] : the current SessionData object
	 */
	protected SessionData doInBackground(SessionData... params) {

		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair(ATrustConstants.paramViewstate, params[0]
				.getViewstate()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramEventValidation, params[0]
				.getEventvalidation()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramInputTan, params[0]
				.getTan()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramBtnSign, params[0]
				.getSignButton()));

		try {

			String url = useTestSignature ? ATrustConstants.requestTestUrl
					: ATrustConstants.requestUrl;
			url += ATrustConstants.signatureExtension + params[0].getSessionID();

			String response = httpCommunicator.post(url, pair);

			try {
				SignatureUtils.checkForErrorResponse(response);
				params[0].setSignature(response);
			} catch (WrongTanException e1) {

				this.e = e1;
				int triesLeft = SignatureUtils.extractNumberOfTriesLeft(e1
						.getMessage());

				if (triesLeft > 0) {
					// Update View state and event validation
					params[0].setEventvalidation(SignatureUtils
							.extractSessionData(response).getEventvalidation());
					params[0].setViewstate(SignatureUtils.extractSessionData(
							response).getViewstate());
					params[0].setSignature(null);

					postBackButtonLocal(params[0]);
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
		if (e != null) {

			activity.handleError(e);
		}
	}

	private void postBackButtonLocal(SessionData sessionData) {

		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		pair.add(new BasicNameValuePair(ATrustConstants.paramViewstate, sessionData
				.getViewstate()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramEventValidation,
				sessionData.getEventvalidation()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramBtnBack, sessionData
				.getBackButton()));

		try {

			String url = useTestSignature ? ATrustConstants.requestTestUrl
					: ATrustConstants.requestUrl;

			url += ATrustConstants.errorExtrension + sessionData.getSessionID()
					+ URL_PARAMETERS;

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

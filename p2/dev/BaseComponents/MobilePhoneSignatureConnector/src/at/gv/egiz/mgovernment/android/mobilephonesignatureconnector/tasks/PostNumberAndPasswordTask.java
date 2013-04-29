package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities.HandySignaturActivity;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.ATrustConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.EgizHttpCommunicatorException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.ParseException;
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
public class PostNumberAndPasswordTask extends
		AsyncTask<SessionData, Void, SessionData> {

	private Exception e = null;
	private HandySignaturActivity activity = null;

	private Boolean useTestSignature = false;

	private HttpCommunicator httpCommunicator = null;

	public PostNumberAndPasswordTask(HttpCommunicator httpCommunicator,
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
		pair.add(new BasicNameValuePair(ATrustConstants.paramPrefix, params[0]
				.getPrefix()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramTelNumber, params[0]
				.getMobilePhoneNumber()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramSignPassword, params[0]
				.getSignaturePassword()));
		pair.add(new BasicNameValuePair(ATrustConstants.paramBtnIdentification,
				params[0].getButtonIdentification()));
		try {
			String url = useTestSignature ? ATrustConstants.requestTestUrl
					: ATrustConstants.requestUrl;
			String response = httpCommunicator.post(
					url + ATrustConstants.identificationExtension
							+ params[0].getSessionID(), pair);

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

				String signatureDataURL = SignatureUtils
						.extractSignatureDataURL(response);
				activity.setSignatureDataURL(signatureDataURL);

			} catch (WrongTanException e1) {
				this.e = e1;
			} catch (SignatureCreationFailedException e1) {
				this.e = e1;
			} catch (WrongUserCredentialsException e1) {
				this.e = e1;
			} catch (PasswordMissingOrTooShortException e1) {
				this.e = e1;
			} catch (ParseException e1) {
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

}

package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.R;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.Constants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.SignatureCreationConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.PasswordMissingOrTooShortException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.StartingParameterException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongPrefixException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongTanException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.WrongUserCredentialsException;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.tasks.PostNumberAndPasswordTask;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.tasks.PostTanTask;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.tasks.RequestSignatureCreationTask;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.HttpCommunicator;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.SessionData;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.util.SignatureUtils;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */
public class HandySignaturActivity extends Activity {

	private HttpCommunicator httpCommunicator = null;
	private Intent startingIntent = null;
	private SessionData sessionData = null;
	private String state = null;

	private Boolean useTestSignature = false;
	private Boolean captureTan = false;

	private String signatureDataURL = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startingIntent = this.getIntent();

		if (startingIntent.hasExtra(SignatureCreationConstants.TEXT_TO_SIGN)) {
			state = SignatureCreationConstants.BEFORE_INITIALIZATION;

			// check if test signature should be used
			useTestSignature = startingIntent
					.getBooleanExtra(
							SignatureCreationConstants.USE_TEST_SIGNATURE,
							Boolean.TRUE);
			
			captureTan = startingIntent.getBooleanExtra(
					SignatureCreationConstants.CAPTURE_TAN, Boolean.FALSE);

		} else {

			setResult(Constants.RESULT_ERROR);
			finish();
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		if (state != null) {

			if (state.equals(SignatureCreationConstants.BEFORE_INITIALIZATION)) {

				initSignatureProvider();
				state = SignatureCreationConstants.INITIALIZED;
				requestSignatureCreation();
			} else if (state.equals(SignatureCreationConstants.INITIALIZED)) {
				requestSignatureCreation();
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == SignatureCreationConstants.SUBMIT_CREDENTIALS_CODE) {
			if (resultCode == RESULT_OK) {

				// check prefix
				String selectedPrefix = intent.getExtras().getString(
						SignatureCreationConstants.PREFIX);
				Resources res = getResources();
				String[] spinnerValues = res
						.getStringArray(R.array.prefix_array);

				if (selectedPrefix.equals(spinnerValues[0])) {

					handleError(new WrongPrefixException(
							"Invalid prefix selected."));
				} else {

					submitCredentials(intent);
				}

			} else if (resultCode == RESULT_CANCELED) {
				cancel();
			} else if (resultCode == Constants.RESULT_RESET_APPLICATION) {
				reset("");
			}
		} else if (requestCode == SignatureCreationConstants.SUBMIT_TAN_CODE) {
			if (resultCode == RESULT_OK) {
				submitTan(intent);
			} else if (resultCode == RESULT_CANCELED) {
				cancel();
			} else if (resultCode == Constants.RESULT_RESET_APPLICATION) {
				reset("");
			}
		}
	}

	private void requestSignatureCreation() {

		RequestSignatureCreationTask requestSignatureCreationTask = new RequestSignatureCreationTask(
				httpCommunicator, this, useTestSignature);

		requestSignatureCreationTask.execute(startingIntent.getExtras()
				.getString(SignatureCreationConstants.TEXT_TO_SIGN));

		try {
			sessionData = requestSignatureCreationTask
					.get(30, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			setResult(Constants.RESULT_ERROR);
			finish();
		} catch (ExecutionException e) {
			setResult(Constants.RESULT_ERROR);
			finish();
		} catch (TimeoutException e) {
			setResult(Constants.RESULT_ERROR);
			finish();
		}

		if (sessionData != null) {

			Intent intent = new Intent(HandySignaturActivity.this,
					SubmitCredentialsActivity.class);

			intent.putExtra(SignatureCreationConstants.USE_TEST_SIGNATURE,
					useTestSignature);

			if (startingIntent.hasExtra(SignatureCreationConstants.ERROR_BOX)) {

				intent.putExtra(
						SignatureCreationConstants.ERROR_BOX,
						startingIntent
								.getStringExtra(SignatureCreationConstants.ERROR_BOX));
			}

			startActivityForResult(intent,
					SignatureCreationConstants.SUBMIT_CREDENTIALS_CODE);
			state = SignatureCreationConstants.REQUESTED_SIGNATURE_CREATION;

		} else {
			setResult(Constants.RESULT_ERROR);
			finish();
		}

	}

	private void submitTan(Intent intent) {

		if (intent.hasExtra(SignatureCreationConstants.TAN)) {

			sessionData.setTan(intent.getExtras().getString(
					SignatureCreationConstants.TAN));

			PostTanTask postTanTask = new PostTanTask(httpCommunicator, this,
					useTestSignature);
			postTanTask.execute(sessionData);

			try {

				this.sessionData = postTanTask.get(30, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				setResult(Constants.RESULT_ERROR);
				finish();
			} catch (ExecutionException e) {
				setResult(Constants.RESULT_ERROR);
				finish();
			} catch (TimeoutException e) {
				setResult(Constants.RESULT_ERROR);
				finish();
			}

			// if signature is null, some error has occurred and we start
			// submitting the TAN again, with the previous state
			if (this.sessionData.getSignature() != null) {

				Intent resultIntent = new Intent();
				resultIntent.putExtra(SignatureCreationConstants.STATE,
						SignatureCreationConstants.FINISHED);
				resultIntent.putExtra(SignatureCreationConstants.SIGNATURE,
						sessionData.getSignature());
				setResult(RESULT_OK, resultIntent);
				state = SignatureCreationConstants.FINISHED;
				finish();
			} 

		} else {
			setResult(Constants.RESULT_ERROR);
			finish();
		}

	}

	private void submitCredentials(Intent intent) {
		if (intent.hasExtra(SignatureCreationConstants.TELEPHONE_NUMBER)
				&& intent
						.hasExtra(SignatureCreationConstants.SIGNATURE_PASSWORD)
				&& intent.hasExtra(SignatureCreationConstants.PREFIX)) {

			sessionData.setMobilePhoneNumber(intent.getExtras().getString(
					SignatureCreationConstants.TELEPHONE_NUMBER));
			sessionData.setSignaturePassword(intent.getExtras().getString(
					SignatureCreationConstants.SIGNATURE_PASSWORD));
			sessionData.setPrefix(intent.getExtras().getString(
					SignatureCreationConstants.PREFIX));

			PostNumberAndPasswordTask postNumberAndPasswordTask = new PostNumberAndPasswordTask(
					httpCommunicator, this, useTestSignature);
			postNumberAndPasswordTask.execute(sessionData);

			try {
				this.sessionData = postNumberAndPasswordTask.get(30,
						TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				setResult(Constants.RESULT_ERROR);
				finish();

			} catch (ExecutionException e) {
				setResult(Constants.RESULT_ERROR);
				finish();

			} catch (TimeoutException e) {
				setResult(Constants.RESULT_ERROR);
				finish();
			}

			startSubmitTanActivity("");
			state = SignatureCreationConstants.SUBMITED_CREDENTIALS;

		} else {
			setResult(Constants.RESULT_ERROR);
			finish();
		}
	}

	public void initSignatureProvider() {

		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpCommunicator = new HttpCommunicator(httpClient);
	}

	public void handleError(Throwable t) {

		if (t instanceof WrongUserCredentialsException) {
			
			reset(getString(R.string.errorWrongNumberOrPassword));

		} else if (t instanceof WrongTanException) {

			// post TAN again if entered TAN was wrong
			if (SignatureUtils.extractNumberOfTriesLeft(t.getMessage()) > 0) {

				int numberOfRetiresLeft = SignatureUtils
						.extractNumberOfTriesLeft(t.getMessage());

			
				startSubmitTanActivity(getString(R.string.errorWrongTAN)
						+ numberOfRetiresLeft
						+ (numberOfRetiresLeft > 1 ? getString(R.string.errorWrongTANTries) : getString(R.string.errorWrongTANTry)));
				return;
			} else {
				reset(getString(R.string.errorWrongTANBlocked));
			}

		} else if (t instanceof PasswordMissingOrTooShortException) {

			// reset application so that user can post credentials again
			reset(getString(R.string.errorMissingPassword));
		} else if (t instanceof WrongPrefixException) {

			reset(getString(R.string.errorChoosePrefix));
		} else {

			reset(getString(R.string.errorUndefined));
		}
		finish();
	}

	// Starts the activity for entering the tan
	private void startSubmitTanActivity(String errorBoxText) {

		if (this.sessionData != null) {

			if (this.sessionData.getReferenceValue() != null) {
				Intent tanIntent = new Intent(HandySignaturActivity.this,
						SubmitTanActivity.class);
				tanIntent.putExtra(SignatureCreationConstants.REFERENCE_VALUE,
						this.sessionData.getReferenceValue());
				tanIntent.putExtra(
						SignatureCreationConstants.USE_TEST_SIGNATURE,
						useTestSignature);
				tanIntent.putExtra(SignatureCreationConstants.CAPTURE_TAN,
						captureTan);
				tanIntent.putExtra(
						SignatureCreationConstants.SIGNATURE_DATA_URL,
						this.signatureDataURL);
				if (errorBoxText != null && errorBoxText != "")
					tanIntent.putExtra(SignatureCreationConstants.ERROR_BOX,
							errorBoxText);
				startActivityForResult(tanIntent,
						SignatureCreationConstants.SUBMIT_TAN_CODE);
			} else {
				setResult(Constants.RESULT_ERROR);
				finish();
			}

		} else {
			setResult(Constants.RESULT_ERROR);
			finish();
		}
	}

	private void reset(String errorBoxText) {
		setResult(Constants.RESULT_RESET_APPLICATION);
		Intent newIntent = startingIntent;
		if (errorBoxText != null && errorBoxText != "")
			newIntent.putExtra(SignatureCreationConstants.ERROR_BOX,
					errorBoxText);
		startActivity(newIntent);
		finish();
	}

	private void cancel() {
		setResult(RESULT_CANCELED);
		state = SignatureCreationConstants.CANCELED;
		finish();
	}

	/**
	 * Creates an Intent object to start the HandySignaturActivity.
	 * 
	 * @param startingContext
	 *            - required
	 * @param certstoreFilename
	 *            - required, path to a keystore that contains the
	 *            a-sign-ssl-03.cer certificate
	 * @param certstorePassword
	 *            - required
	 * @param truststoreFilename
	 *            - required, path a keystore that contains the
	 *            a-trust-nqual-03.cer certificate
	 * @param truststorePassword
	 *            - required
	 * @param contentToSign
	 *            - optional
	 * @param transformsInfoMimeType
	 *            - optional, default value: application/xml
	 * @param useTestSignature
	 *            - required
	 * @param captureTan
	 *            - required
	 * @param packageName
	 *            of the calling application - required for reading out resource
	 *            files (in our case truststore and certstore)
	 * @return the Intent to start this Activity
	 * @throws StartingParameterException
	 *             if one of the required parameters is not given
	 */
	public static Intent buildStartingIntent(Context startingContext,
			String contentToSign, boolean useTestSignature, boolean captureTan)
			throws StartingParameterException {

		Intent intent = new Intent(startingContext, HandySignaturActivity.class);

		intent.putExtra(SignatureCreationConstants.STATE,
				SignatureCreationConstants.BEFORE_INITIALIZATION);

		if (contentToSign == null)
			contentToSign = "";
		intent.putExtra(SignatureCreationConstants.TEXT_TO_SIGN, contentToSign);

		intent.putExtra(SignatureCreationConstants.CAPTURE_TAN, captureTan);

		intent.putExtra(SignatureCreationConstants.USE_TEST_SIGNATURE,
				useTestSignature);
		return intent;

	}

	public String getSignatureDataURL() {
		return signatureDataURL;
	}

	public void setSignatureDataURL(String signatureDataURL) {

		this.signatureDataURL = signatureDataURL;
	}

}

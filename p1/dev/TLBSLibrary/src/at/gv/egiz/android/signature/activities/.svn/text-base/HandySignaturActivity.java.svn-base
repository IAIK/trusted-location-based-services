package at.gv.egiz.android.signature.activities;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import at.gv.egiz.android.R;
import at.gv.egiz.android.application.Constants;
import at.gv.egiz.android.communication.http.CustomHttpClient;
import at.gv.egiz.android.communication.http.HttpCommunicator;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.signature.SessionData;
import at.gv.egiz.android.signature.SignatureCreationConstants;
import at.gv.egiz.android.signature.SignatureUtils;
import at.gv.egiz.android.signature.exception.PasswordMissingOrTooShortException;
import at.gv.egiz.android.signature.exception.StartingParameterException;
import at.gv.egiz.android.signature.exception.WrongTanException;
import at.gv.egiz.android.signature.exception.WrongUserCredentialsException;
import at.gv.egiz.android.signature.tasks.PostNumberAndPasswordTask;
import at.gv.egiz.android.signature.tasks.PostTanTask;
import at.gv.egiz.android.signature.tasks.RequestSignatureCreationTask;
import at.gv.egiz.android.util.StorageUtils;

/**
 * 
 * @author Sandra Kreuzhuber
 * 
 */
public class HandySignaturActivity extends Activity {

	private HttpCommunicator httpCommunicator = null;
	private Intent startingIntent = null;
	private SessionData sessionData = null;
	private String tag = DebugTags.EGIZ + " " + DebugTags.SIGNATURE
			+ " HandySignaturActivity";
	private String state = null;

	private Boolean useTestSignature = false;
	private Boolean captureTan = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(tag, "onCreate()");

		startingIntent = this.getIntent();

		if (startingIntent
				.hasExtra(SignatureCreationConstants.CERTSTORE_FILENAME)
				&& startingIntent
						.hasExtra(SignatureCreationConstants.CERTSTORE_PASSWORD)
				&& startingIntent
						.hasExtra(SignatureCreationConstants.TRUSTSTORE_FILENAME)
				&& startingIntent
						.hasExtra(SignatureCreationConstants.TRUSTSTORE_PASSWORD)
				&& startingIntent
						.hasExtra(SignatureCreationConstants.PACKAGE_NAME)
				&& startingIntent
						.hasExtra(SignatureCreationConstants.TEXT_TO_SIGN)
				&& startingIntent
						.hasExtra(SignatureCreationConstants.TRANSFORMS_INFO_MIME_TYPE)) {
			state = SignatureCreationConstants.BEFORE_INITIALIZATION;

			// check if test signature should be used
			useTestSignature = startingIntent
					.getBooleanExtra(
							SignatureCreationConstants.USE_TEST_SIGNATURE,
							Boolean.TRUE);
			captureTan = startingIntent.getBooleanExtra(
					SignatureCreationConstants.CAPTURE_TAN, Boolean.FALSE);

			// TODO saveInstanceState
		} else {
			reset(getString(R.string.signature_error_unkownerror));
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		if (state != null) {
			Log.d(tag, "onStart() with State: " + state);
			if (state.equals(SignatureCreationConstants.BEFORE_INITIALIZATION)) {
				initSignatureProvider(
						startingIntent.getExtras().getString(
								SignatureCreationConstants.CERTSTORE_FILENAME),
						startingIntent.getExtras().getString(
								SignatureCreationConstants.CERTSTORE_PASSWORD),
						startingIntent.getExtras().getString(
								SignatureCreationConstants.TRUSTSTORE_FILENAME),
						startingIntent.getExtras().getString(
								SignatureCreationConstants.TRUSTSTORE_PASSWORD),
						startingIntent.getExtras().getString(
								SignatureCreationConstants.PACKAGE_NAME));
				state = SignatureCreationConstants.INITIALIZED;
				requestSignatureCreation();
			} else if (state.equals(SignatureCreationConstants.INITIALIZED)) {
				requestSignatureCreation();
			}
			// (state.equals(SignatureCreation.SUBMIT_CREDENTIALS_CODE)) {
			// startSubmitTanActivity();
			// } else if (state.equals(SignatureCreation.SUBMIT_TAN_CODE))
			// startS
		}

	}

	@Override
	public void onRestart() {
		super.onRestart();
		Log.i(tag, "onRestart()");

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(tag, "onPause()");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(tag, "onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(tag, "onDestroy()");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(tag, "onResume()");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d(tag, "onActivityResult()");
		if (requestCode == SignatureCreationConstants.SUBMIT_CREDENTIALS_CODE) {
			if (resultCode == RESULT_OK) {

				Log.d(tag, "SubmitCredentials Okay");
				submitCredentials(intent);

			} else if (resultCode == RESULT_CANCELED) {
				cancel();
			} else if (resultCode == Constants.RESULT_RESET_APPLICATION) {
				reset("");
			}
		} else if (requestCode == SignatureCreationConstants.SUBMIT_TAN_CODE) {
			if (resultCode == RESULT_OK) {
				Log.d(tag, "SubmitTan Okay");
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

		requestSignatureCreationTask.execute(
				startingIntent.getExtras().getString(
						SignatureCreationConstants.TEXT_TO_SIGN),
				startingIntent.getExtras().getString(
						SignatureCreationConstants.TRANSFORMS_INFO_MIME_TYPE));

		try {
			sessionData = requestSignatureCreationTask
					.get(30, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			reset(getString(R.string.signature_error_unkownerror));
		} catch (ExecutionException e) {
			reset(getString(R.string.signature_error_unkownerror));
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (sessionData != null) {
			Log.d(tag,
					"Successfully sent request for signature creation. Session id: "
							+ sessionData.getSessionID());
			Intent intent = new Intent(HandySignaturActivity.this,
					SubmitCredentialsActivity.class);
			intent.putExtra(SignatureCreationConstants.USE_TEST_SIGNATURE,
					useTestSignature);
			// add error message to intent if there exists one
			if (startingIntent.hasExtra(SignatureCreationConstants.ERROR_BOX)) {
				Log.d(tag,
						"Contains error box. Message: "
								+ startingIntent
										.getStringExtra(SignatureCreationConstants.ERROR_BOX));
				intent.putExtra(
						SignatureCreationConstants.ERROR_BOX,
						startingIntent
								.getStringExtra(SignatureCreationConstants.ERROR_BOX));
			}
			startActivityForResult(intent,
					SignatureCreationConstants.SUBMIT_CREDENTIALS_CODE);
			state = SignatureCreationConstants.REQUESTED_SIGNATURE_CREATION;
		} else {
			reset(getString(R.string.signature_error_unkownerror));
		}

	}

	private void submitTan(Intent intent) {
		if (intent.hasExtra(SignatureCreationConstants.TAN)) {
			sessionData.setTan(intent.getExtras().getString(
					SignatureCreationConstants.TAN));
			Log.d(tag,
					"Tan: "
							+ intent.getExtras().getString(
									SignatureCreationConstants.TAN));
			PostTanTask postTanTask = new PostTanTask(httpCommunicator, this,
					useTestSignature);
			postTanTask.execute(sessionData);

			try {

				this.sessionData = postTanTask.get(30, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// if signature is null, some error has occurred and we start
			// submitting the tan again, with the previous state
			if (this.sessionData.getSignature() != null) {

				Log.d(tag,
						"Successfully retrieved signature: "
								+ sessionData.getSignature());
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
			reset(getString(R.string.signature_error_unkownerror));
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			startSubmitTanActivity("");
			state = SignatureCreationConstants.SUBMITED_CREDENTIALS;

		} else {
			reset(getString(R.string.signature_error_unkownerror));
		}
	}

	public void initSignatureProvider(String filenameCertstore,
			String pwdCertstore, String filenameTruststore,
			String pwdTruststore, String packageName) {
		InputStream certstore = StorageUtils.loadFileFromRawResources(
				filenameCertstore, getApplicationContext(), packageName);
		InputStream truststore = StorageUtils.loadFileFromRawResources(
				filenameTruststore, getApplicationContext(), packageName);
		DefaultHttpClient httpClient = new CustomHttpClient(certstore,
				pwdCertstore, truststore, pwdTruststore);
		// DefaultHttpClient httpClient = new DefaultHttpClient();
		httpCommunicator = new HttpCommunicator(httpClient);
	}

	public void handleError(Throwable t) {

		Log.d(tag, "handleError(): " + t.getMessage());

		if (t instanceof WrongUserCredentialsException) {
			// reset signature creation if telephonenumber or password is wrong
			reset(getString(R.string.signature_error_wrongusercredentials));

		} else if (t instanceof WrongTanException) {
			// post tan again if entered tan was false
			Log.d(tag, "Try to resend the TAN.");
			if (SignatureUtils.extractNumberOfTriesLeft(t.getMessage()) > 0) {
				startSubmitTanActivity(getString(
						R.string.signature_error_wrongtanamountoftriesleft,
						SignatureUtils.extractNumberOfTriesLeft(t.getMessage())));
				return; // very important!!! - stupid mistake...
			} else {
				reset(getString(R.string.signature_error_3wrongtansentered));
			}

		} else if (t instanceof PasswordMissingOrTooShortException) {
			// reset application so that user can post credentials again
			reset(getString(R.string.signature_error_pwmissingortooshort));
		} else {
			// other error has occurred, reset the activity
			reset(getString(R.string.signature_error_unkownerror));
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
				if (errorBoxText != null && errorBoxText != "")
					tanIntent.putExtra(SignatureCreationConstants.ERROR_BOX,
							errorBoxText);
				startActivityForResult(tanIntent,
						SignatureCreationConstants.SUBMIT_TAN_CODE);
			} else {
				reset(getString(R.string.signature_error_unkownerror));
			}

		} else {
			reset(getString(R.string.signature_error_unkownerror));
		}
	}

	private void reset(String errorBoxText) {
		Log.d(tag, "reset the creation process...");
		Intent resultIntent = new Intent();
		resultIntent.putExtra(SignatureCreationConstants.STATE,
				SignatureCreationConstants.CANCELED);
		resultIntent.putExtra(SignatureCreationConstants.ERROR_BOX,
				errorBoxText);
		resultIntent
				.putExtra(
						SignatureCreationConstants.TEXT_TO_SIGN,
						startingIntent
								.getStringExtra(SignatureCreationConstants.TEXT_TO_SIGN));
		resultIntent.putExtra(SignatureCreationConstants.USE_TEST_SIGNATURE,
				startingIntent.getBooleanExtra(
						SignatureCreationConstants.USE_TEST_SIGNATURE,
						Boolean.FALSE));
		resultIntent.putExtra(SignatureCreationConstants.CAPTURE_TAN,
				startingIntent.getBooleanExtra(
						SignatureCreationConstants.CAPTURE_TAN, Boolean.FALSE));
		setResult(RESULT_CANCELED, resultIntent);
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
			String certstoreFilename, String certstorePassword,
			String truststoreFilename, String truststorePassword,
			String packageName, String contentToSign,
			String transformsInfoMimeType, boolean useTestSignature,
			boolean captureTan) throws StartingParameterException {

		Intent intent = new Intent(startingContext, HandySignaturActivity.class);

		intent.putExtra(SignatureCreationConstants.STATE,
				SignatureCreationConstants.BEFORE_INITIALIZATION);
		if (certstoreFilename != null && certstoreFilename != "")
			intent.putExtra(SignatureCreationConstants.CERTSTORE_FILENAME,
					certstoreFilename);
		else
			throw new StartingParameterException(
					"You have to specify the filename of the certstore.");

		if (certstorePassword != null && certstorePassword != "")
			intent.putExtra(SignatureCreationConstants.CERTSTORE_PASSWORD,
					certstorePassword);
		else
			throw new StartingParameterException(
					"You have to specify the password of the certstore.");

		if (truststoreFilename != null && truststoreFilename != "")
			intent.putExtra(SignatureCreationConstants.TRUSTSTORE_FILENAME,
					truststoreFilename);
		else
			throw new StartingParameterException(
					"You have to specify the filename of the truststore.");

		if (truststorePassword != null && truststorePassword != "")
			intent.putExtra(SignatureCreationConstants.TRUSTSTORE_PASSWORD,
					truststorePassword);
		else
			throw new StartingParameterException(
					"You have to specify the password of the truststore.");

		if (packageName != null && packageName != "")
			intent.putExtra(SignatureCreationConstants.PACKAGE_NAME,
					packageName);
		else
			throw new StartingParameterException(
					"You have to specify the package name of the calling application.");

		if (contentToSign == null)
			contentToSign = "";
		intent.putExtra(SignatureCreationConstants.TEXT_TO_SIGN, contentToSign);

		if (transformsInfoMimeType == null || transformsInfoMimeType == "")
			transformsInfoMimeType = "application/xml";
		intent.putExtra(SignatureCreationConstants.TRANSFORMS_INFO_MIME_TYPE,
				transformsInfoMimeType);

		intent.putExtra(SignatureCreationConstants.CAPTURE_TAN, captureTan);

		intent.putExtra(SignatureCreationConstants.USE_TEST_SIGNATURE,
				useTestSignature);
		return intent;

	}

}
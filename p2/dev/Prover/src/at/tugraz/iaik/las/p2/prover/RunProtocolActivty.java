package at.tugraz.iaik.las.p2.prover;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities.HandySignaturActivity;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.Constants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.SignatureCreationConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.exceptions.StartingParameterException;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.R;

import at.tugraz.iaik.las.p2.prover.cryptotag.ICryptoTag;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;
import at.tugraz.iaik.las.p2.ttp.server.GetNonceResponse;
import at.tugraz.iaik.las.p2.ttp.server.GetLttResponse;
import at.tugraz.iaik.las.p2.ttp.server.GetTLttResponse;
import at.tugraz.iaik.las.p2.ttp.server.TtpApi;
import at.tugraz.iaik.las.p2.ttp.server.TtpApiUtils;
import at.tugraz.iaik.las.p2.common.TagCrypto;

/**
 * The activity that handles the complete protocol and displays the status
 * information.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class RunProtocolActivty extends Activity {

	private ProtocolState state = ProtocolState.READY;

	private TLttManager tLttManager;

	// used to exchange information across activities
	private static ICryptoTag tag;

	// information gathered while running the protocol
	ProtocolSession protocolSession;

	private TextView tvStatus;
	private ProgressBar progressBar;
	private Button btViewTLtt;

	private boolean verboseLog = false;

	public static void init(ICryptoTag tag) {
		RunProtocolActivty.tag = tag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.protocol_status);

		this.tvStatus = (TextView) this.findViewById(R.id.tvStatus);
		this.tvStatus.setText("");
		this.progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
		this.progressBar.setMax(7);
		this.btViewTLtt = (Button) this.findViewById(R.id.btViewTLtt);
		this.btViewTLtt.setEnabled(false);

		this.protocolSession = new ProtocolSession();
		this.tLttManager = new TLttManager(ProverApp.extUsbDataDirectory);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		this.verboseLog = sharedPref.getBoolean("verboseLog", true);

		this.nextProtocolStep();
	}

	private void nextProtocolStep() {
		this.protocolShowOutputVerbose("----------------------");
		switch (this.state) {
		case READY:
			this.progressBar.setProgress(1);
			this.state = ProtocolState.GET_UID_FROM_TAG;
			this.protocolGetUidFromTag();
			break;
		case GET_UID_FROM_TAG:
			this.progressBar.setProgress(2);
			this.state = ProtocolState.GET_NONCE_FROM_TTP;
			this.protocolGetNonceFromTtp();
			break;
		case GET_NONCE_FROM_TTP:
			this.progressBar.setProgress(3);
			this.state = ProtocolState.GET_SIGNED_NONCE_FROM_TAG;
			this.protocolGetSignedNonceFromTag();
			break;
		case GET_SIGNED_NONCE_FROM_TAG:
			this.progressBar.setProgress(4);
			this.state = ProtocolState.GET_LTT_FROM_TTP;
			this.protocolGetLttFromTtp();
			break;
		case GET_LTT_FROM_TTP:
			this.progressBar.setProgress(5);
			this.state = ProtocolState.GET_LTT_SIGNED_BY_MOBILE_PHONE_SIGNATURE;
			this.protoclGetLttSignedByMobilePhoneSignature();
			break;
		case GET_LTT_SIGNED_BY_MOBILE_PHONE_SIGNATURE:
			this.progressBar.setProgress(6);
			this.state = ProtocolState.GET_TLTT_FROM_TTP;
			this.protocolGetTLttFromTtp();
			break;
		case GET_TLTT_FROM_TTP:
			this.progressBar.setProgress(7);
			this.state = ProtocolState.DONE;
			this.protocolDone();
		}
	}

	private void protocolGetUidFromTag() {
		this.protocolShowOutput("[1] Reading UID from Tag...");

		AsyncTask<Void, Void, byte[]> task = new AsyncTask<Void, Void, byte[]>() {

			@Override
			protected byte[] doInBackground(Void... params) {
				return RunProtocolActivty.tag.getUid();
			}

			@Override
			protected void onPostExecute(byte[] uid) {
				if (uid.length != TagCrypto.CRYPTO_TAG_UID_LENGTH) {
					RunProtocolActivty.this
							.protocolShowError("Could not read UI from Tag");
				} else {
					RunProtocolActivty.this.protocolShowOutputVerbose(String.format(
							"UID read from Tag: %s",
							Utils.byteArrayToHexString(uid)));
					RunProtocolActivty.this.protocolSession.Uid = uid;
					RunProtocolActivty.this.nextProtocolStep();
				}
			}

		};

		task.execute();
	}

	private void protocolShowError(String message) {
		this.tvStatus.append(String.format("%s\n", message));
	}

	private void protocolShowOutput(String message) {
		this.tvStatus.append(String.format("%s\n", message));
	}

	private void protocolShowOutputVerbose(String message) {
		if (this.verboseLog) {
			this.tvStatus.append(String.format("%s\n", message));
		}
	}

	private void protocolGetNonceFromTtp() {
		this.protocolShowOutput("[2] Requesting Nonce from TTP...");

		TtpApiAsyncTask<GetNonceResponse> task = new TtpApiAsyncTask<GetNonceResponse>(
				this.protocolSession) {

			@Override
			protected GetNonceResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getNonce(protocolSession.Uid);
			}

			@Override
			protected void onSuccess(GetNonceResponse response,
					ProtocolSession protocolSession) {
				RunProtocolActivty.this.protocolShowOutputVerbose(String.format(
						"Nonce received from TTP: %s",
						Utils.byteArrayToHexString(response.Nonce)));
				protocolSession.Nonce = response.Nonce;
				RunProtocolActivty.this.nextProtocolStep();
			}

			@Override
			protected void onError(GetNonceResponse response) {
				RunProtocolActivty.this.protocolShowError(response.Message);
			}

			@Override
			protected void onException(Exception e2) {
				RunProtocolActivty.this
						.protocolShowError("Could not contact TTP at "
								+ ProxyFactory.lastUsedApiUrl);
			}
		};

		task.execute();
	}

	private void protocolGetSignedNonceFromTag() {
		this.protocolShowOutput("[3] Getting Nonce signed by Tag...");

		AsyncTask<Void, Void, byte[]> task = new AsyncTask<Void, Void, byte[]>() {

			@Override
			protected byte[] doInBackground(Void... params) {
				return RunProtocolActivty.tag
						.sign(RunProtocolActivty.this.protocolSession.Nonce);
			}

			@Override
			protected void onPostExecute(byte[] signedNonce) {
				if (signedNonce == null || signedNonce.length <= 0) {
					RunProtocolActivty.this
							.protocolShowError("Could not get Nonce signed by Tag.");
				} else {
					RunProtocolActivty.this.protocolShowOutputVerbose(String.format(
							"Signed Nonce from Tag: %s",
							Utils.byteArrayToHexString(signedNonce)));
					RunProtocolActivty.this.protocolSession.SignedNonce = signedNonce;
					RunProtocolActivty.this.nextProtocolStep();
				}
			}
		};

		task.execute();
	}

	private void protocolGetLttFromTtp() {
		this.protocolShowOutput("[4] Requesting LTT from TTP...");

		TtpApiAsyncTask<GetLttResponse> task = new TtpApiAsyncTask<GetLttResponse>(
				this.protocolSession) {

			@Override
			protected void onSuccess(GetLttResponse response,
					ProtocolSession protocolSession) {
				protocolShowOutputVerbose(String.format("LTT received from TTP: %s",
						TtpApiUtils.SerializeToXmlString(response.Ltt)));
				protocolSession.Ltt = response.Ltt;
				RunProtocolActivty.this.nextProtocolStep();
			}

			@Override
			protected void onException(Exception e2) {
				RunProtocolActivty.this
						.protocolShowError("Could not contact TTP at "
								+ ProxyFactory.lastUsedApiUrl);
			}

			@Override
			protected void onError(GetLttResponse response) {
				RunProtocolActivty.this
						.protocolShowError("Could not get LTT from TTP: "
								+ response.Message);
			}

			@Override
			protected GetLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getLtt(protocolSession.Nonce,
						protocolSession.SignedNonce);
			}
		};

		task.execute();
	}

	private void protoclGetLttSignedByMobilePhoneSignature() {
		this.protocolShowOutput("[5] Requesting Mobile Phone Signature...");

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean useTestSignature = sharedPref.getBoolean("useTestSignature",
				true);
		Boolean captureTan = sharedPref.getBoolean("captureTan", true);
		this.startSignatureCreation(
				TtpApiUtils.SerializeToXmlString(this.protocolSession.Ltt),
				captureTan, useTestSignature);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == SignatureCreationConstants.CREATE_SIGNATURE) {

			if (resultCode == RESULT_OK
					&& intent.hasExtra(SignatureCreationConstants.SIGNATURE)) {

				String signature = intent.getExtras().getString(
						SignatureCreationConstants.SIGNATURE);
				if (signature != null) {

					this.protocolShowOutputVerbose(signature);
					this.protocolSession.SignedLttAsXmlString = signature;
					this.nextProtocolStep();
				}
			} else if (resultCode == RESULT_CANCELED) {
				this.protocolShowOutputVerbose("User cancelled Mobile Phone Signature process.");

			} else if (resultCode == Constants.RESULT_ERROR) {
				this.protocolShowOutputVerbose("Error during Mobile Phone Signature process.");

			} else {
				this.protocolShowOutputVerbose("Error during Mobile Phone Signature process.");
			}
		}
	}

	private void protocolGetTLttFromTtp() {
		this.protocolShowOutput("[6] Requesting T-LTT from TTP...");

		TtpApiAsyncTask<GetTLttResponse> task = new TtpApiAsyncTask<GetTLttResponse>(
				this.protocolSession) {

			@Override
			protected void onSuccess(GetTLttResponse response,
					ProtocolSession protocolSession) {
				protocolSession.TLttAsXmlString = response.TLttXmlString;
				RunProtocolActivty.this.protocolShowOutputVerbose(String.format(
						"T-LTT received from TTP: \n%s",
						protocolSession.TLttAsXmlString));
				RunProtocolActivty.this.nextProtocolStep();
			}

			@Override
			protected void onException(Exception e2) {
				RunProtocolActivty.this
						.protocolShowError("Could not contact TTP at "
								+ ProxyFactory.lastUsedApiUrl);
			}

			@Override
			protected void onError(GetTLttResponse response) {
				RunProtocolActivty.this
						.protocolShowError("Could not get T-LTT from TTP: "
								+ response.Message);
			}

			@Override
			protected GetTLttResponse doApiCall(TtpApi api,
					ProtocolSession protocolSession) {
				return api.getTLtt(protocolSession.SignedLttAsXmlString,
						protocolSession.Nonce);
			}
		};

		task.execute();
	}

	@SuppressLint("SimpleDateFormat")
	private void protocolDone() {
		this.protocolShowOutput("[7] Successfully acquired T-LTT.");
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String path = this.tLttManager.saveTLtt(//
				String.format("TLTT_%s.xml", sdf.format(now)),//
				this.protocolSession.TLttAsXmlString);
		if (path == null || path.length() <= 0) {
			this.protocolShowOutput("Could not save T-LTT on phone.");
		} else {
			this.protocolShowOutput(String.format("T-LTT saved in %s", path));
			this.btViewTLtt.setEnabled(true);
			this.protocolSession.TLttPath = path;
		}
	}
	
	public void onViewTLttClick(View v) {
        File file = new File(this.protocolSession.TLttPath);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "text/xml");
        startActivity(intent);
    }

	private void startSignatureCreation(String contentToSign,
			Boolean captureTan, Boolean useTestSignature) {
		Intent intent;
		try {
			intent = HandySignaturActivity.buildStartingIntent(
					RunProtocolActivty.this, contentToSign, useTestSignature,
					captureTan);
			startActivityForResult(intent,
					SignatureCreationConstants.CREATE_SIGNATURE);
		} catch (StartingParameterException e) {
			Log.e(ProverApp.P,
					"Missing Parameter. Cannot start Mobile Phone Signature creation process.");
		}
	}
}

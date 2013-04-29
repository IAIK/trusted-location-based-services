package at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.R;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.ATrustConstants;
import at.gv.egiz.mgovernment.android.mobilephonesignatureconnector.constants.SignatureCreationConstants;

/**
 * 
 * @author Sandra Kreuzhuber
 * @author Thomas Zefferer
 * 
 */
public class SubmitTanActivity extends Activity {

	private EditText tanField;
	private TextView referenceValueTextView;
	private Boolean useTestSignature = false;
	private Boolean captureTan = false;
	private BroadcastReceiver mTanReceiver = null;
	private TextView errorBox;
	private ImageButton signButton;
	
	private String signatureDataURL = null;
	
	private static final int SMS_RECEIVER_PRIORITY = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.egiz_enter_tan);

		tanField = (EditText) findViewById(R.id.editTextTan);
		referenceValueTextView = (TextView) findViewById(R.id.TextView04);

		Bundle bundle = this.getIntent().getExtras();
		captureTan = bundle.getBoolean(SignatureCreationConstants.CAPTURE_TAN,
				Boolean.FALSE);		
	
		signatureDataURL = bundle.getString(SignatureCreationConstants.SIGNATURE_DATA_URL);
		
		// if the test signature is in use, set the TAN field
		useTestSignature = bundle.getBoolean(
				SignatureCreationConstants.USE_TEST_SIGNATURE, Boolean.FALSE);		
		
		signButton = (ImageButton) findViewById(R.id.buttonSignieren);
		
		if(captureTan || useTestSignature) {
			signButton.setFocusable(Boolean.TRUE);
			signButton.setFocusableInTouchMode(Boolean.TRUE);
			signButton.requestFocus();
		}

		if (bundle.containsKey(SignatureCreationConstants.REFERENCE_VALUE)) {
			String referenceValue = bundle
					.getString(SignatureCreationConstants.REFERENCE_VALUE);

			referenceValueTextView.setText(referenceValue);
		}

		if (bundle.containsKey(SignatureCreationConstants.ERROR_BOX)) {
			errorBox = (TextView) findViewById(R.id.Errorbox);
			errorBox.setText(bundle
					.getString(SignatureCreationConstants.ERROR_BOX));
			errorBox.setVisibility(View.VISIBLE);
		}

		if (useTestSignature) {
			tanField.setText(getString(R.string.testSignatureTAN));
		} else if (captureTan) {
			tanField.setText(getString(R.string.TANWaitMessage));

			// register a broadcast receiver for the mTAN
			final IntentFilter filter = new IntentFilter();
			filter.addAction("android.provider.Telephony.SMS_RECEIVED");
			filter.setPriority(SMS_RECEIVER_PRIORITY); // high enough to get intent first
			this.mTanReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					handleIncomingSMS(intent, this);
				}
			};

			// Registers the receiver so that activity will listen for
			// broadcasts for incoming SMS
			this.registerReceiver(this.mTanReceiver, filter);
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTanReceiver != null)
			this.unregisterReceiver(this.mTanReceiver);
	}


	public void submit(View view) {

		Intent intent = new Intent();
		intent.putExtra(SignatureCreationConstants.TAN, tanField.getText()
				.toString());
		setResult(RESULT_OK, intent);
		finish();

	}

	public void showSignatureData(View view) {
		
		if(signatureDataURL != null) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(signatureDataURL));
			startActivity(browserIntent);
		} 		
	}
	
	public void cancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}

	private void handleIncomingSMS(Intent intent,
			BroadcastReceiver broadcastReceiver) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String sms_content = "";
		String sender = "";
		if (bundle != null) {
			// extract received message
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];

			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				sms_content += msgs[i].getMessageBody().toString();
				sender = msgs[i].getOriginatingAddress();
			}

			if (sender.equals(ATrustConstants.mTanSender)) {
				broadcastReceiver.abortBroadcast();

				String tan = sms_content.substring(47, 53);
				tanField.setText(tan);				
				signButton.requestFocus();
			}
		}
	}
}
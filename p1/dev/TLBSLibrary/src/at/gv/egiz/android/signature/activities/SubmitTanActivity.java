package at.gv.egiz.android.signature.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import at.gv.egiz.android.R;
import at.gv.egiz.android.application.Constants;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.signature.SignatureCreationConstants;

/**
 * Provides the User Interface for entering the mTAN. Intent should contain:
 * TODO
 * 
 * @author sandra
 * 
 */
public class SubmitTanActivity extends Activity {

	private EditText tanField;
	private TextView referenceValueTextView;
	private Boolean useTestSignature = false;
	private Boolean captureTan = false;
	private BroadcastReceiver mTanReceiver = null;
	private TextView errorBox;

	private static final String TAG = DebugTags.EGIZ + " "
			+ DebugTags.SIGNATURE + " SubmitTanActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.egiz_enter_tan);

		tanField = (EditText) findViewById(R.id.editTextTan);
		referenceValueTextView = (TextView) findViewById(R.id.TextView04);

		Bundle bundle = this.getIntent().getExtras();

		if (bundle.containsKey(SignatureCreationConstants.REFERENCE_VALUE)) {
			String referenceValue = bundle
					.getString(SignatureCreationConstants.REFERENCE_VALUE);

			referenceValueTextView.setText(referenceValue);
		}

		captureTan = bundle.getBoolean(SignatureCreationConstants.CAPTURE_TAN,
				Boolean.FALSE);

		// if the test signature is in use, set the tan field
		useTestSignature = bundle.getBoolean(
				SignatureCreationConstants.USE_TEST_SIGNATURE, Boolean.FALSE);

		if (bundle.containsKey(SignatureCreationConstants.ERROR_BOX)) {
			errorBox = (TextView) findViewById(R.id.Errorbox);
			errorBox.setText(bundle
					.getString(SignatureCreationConstants.ERROR_BOX));
			errorBox.setVisibility(View.VISIBLE);
		}

		if (useTestSignature) {
			tanField.setText("123456");
		} else if (captureTan) {
			tanField.setText(getString(R.string.signature_waiting));

			// register a broadcast receiver for the mTan
			final IntentFilter filter = new IntentFilter();
			filter.addAction("android.provider.Telephony.SMS_RECEIVED");
			filter.setPriority(100); // high enough to get intent first
			this.mTanReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					handleIncomingSMS(intent, this);
				}

			};

			// Registers the receiver so that activity will listen for
			// broadcasts for incoming sms
			this.registerReceiver(this.mTanReceiver, filter);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTanReceiver != null)
			this.unregisterReceiver(this.mTanReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.general_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.reset) {
			setResult(Constants.RESULT_RESET_APPLICATION);
			finish();
			return true;
		} else if (item.getItemId() == R.id.quit) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else
			return super.onOptionsItemSelected(item);

	}

	public void submit(View view) {

		Intent intent = new Intent();
		intent.putExtra(SignatureCreationConstants.TAN, tanField.getText()
				.toString());
		setResult(RESULT_OK, intent);
		finish();

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
			Log.d(TAG, "Received SMS:  \"" + sms_content + "\"");
			Log.d(TAG, "Sender: " + sender);
			if (sender.equals("A-Trust")) {
				broadcastReceiver.abortBroadcast();
				Log.d(TAG,
						"Message with TAN from A-Trust arrived. TAN is captured.");
				String tan = sms_content.substring(47, 53);
				Log.d(TAG, "Tan is: " + tan);
				tanField.setText(tan);
			}

		}

	}
}

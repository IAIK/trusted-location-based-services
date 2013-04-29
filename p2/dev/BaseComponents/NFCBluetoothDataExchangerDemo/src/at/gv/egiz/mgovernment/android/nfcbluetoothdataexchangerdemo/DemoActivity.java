package at.gv.egiz.mgovernment.android.nfcbluetoothdataexchangerdemo;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.gv.egiz.mgovernment.android.nfcbluetoothdataexchanger.bluetooth.BluetoothDataExchanger;

/**
 * 
 * @author Thomas Zefferer
 * 
 */


public class DemoActivity extends Activity {

	private static final int REQUEST_CODE_SEND_DATA = 1001;
	private static final int REQUEST_CODE_RECEIVE_DATA = 1002;

	private static final String TAG = "DemoActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_activity);

		final EditText textArea = (EditText) findViewById(R.id.textArea);
		final Button sendButton = (Button) findViewById(R.id.sendButton);

		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				sendData(textArea.getText().toString());
			}
		});

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {

			// We got called by NFC -> redirect to BluetoothDataExchanger
			Intent intent = new Intent(DemoActivity.this,
					BluetoothDataExchanger.class);
			intent.putExtra(BluetoothDataExchanger.CALLING_INTENT_ID,
					getIntent());
			startActivityForResult(intent, REQUEST_CODE_RECEIVE_DATA);
		}

	}

	private void sendData(String text) {

		Intent intent = new Intent(DemoActivity.this,
				BluetoothDataExchanger.class);
		intent.putExtra(BluetoothDataExchanger.DATA_TO_SEND_ID, text);
		startActivityForResult(intent, REQUEST_CODE_SEND_DATA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		switch (requestCode) {
		case REQUEST_CODE_SEND_DATA:

			Log.d(TAG, "Back from sending data. Result: " + resultCode);
			break;
		case REQUEST_CODE_RECEIVE_DATA:

			Log.d(TAG, "Back from receiving data. Result: " + resultCode);

			if (intent.getExtras().containsKey(
					BluetoothDataExchanger.READ_TEXT_ID)) {

				final EditText textArea = (EditText) findViewById(R.id.textArea);
				textArea.setText(intent.getExtras().getString(
						BluetoothDataExchanger.READ_TEXT_ID));

			} else {

				Log.w(TAG, "No read text found.");
			}

			break;
		default:
			// do nothing
			break;
		}
	}
}

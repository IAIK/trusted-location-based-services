package at.gv.egiz.mgovernment.android.nfcbluetoothdataexchanger.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import at.gv.egiz.mgovernment.android.nfcbluetoothdataexchanger.R;

/**
 * 
 * @author Thomas Zefferer
 * 
 */

public class BluetoothDataExchanger extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	// Debugging
	private static final String TAG = "BluetoothDataExchanger";
	private static final boolean D = false;

	// Message types sent from the BluetoothCommunicationService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_ABORT = 6;

	// Key names received from the BluetoothCommunicationService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request and return codes
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int MESSAGE_SENT = 1;
	public static final String READ_TEXT_ID = "readText";
	public static final String CALLING_INTENT_ID = "callingIntent";
	public static final String DATA_TO_SEND_ID = "data2send";
	
	
	// NFC mime type
	private static final String NFC_MIME_TYPE = "application/at.gv.egiz.mgovernment.android.nfcbluetoothdataexchanger";

	// Name of the connected device
	private String connectedDeviceName = null;

	// Local Bluetooth adapter
	private BluetoothAdapter bluetoothAdapter = null;
	// Member object for the communication services
	private BluetoothCommunicationService bluetoothCommunicationService = null;

	// Local NFC adapter
	private NfcAdapter mNfcAdapter;

	// Local communication properties
	private String remoteMACAddress;
	private String textToSend = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.main);

		// Get local Bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (bluetoothAdapter == null) {
			Toast.makeText(this, getString(R.string.bt_not_available),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

	}

	private void beamMACAddress() {

		if (D)
			Log.d(TAG, "Beaming MAC address..");

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, getString(R.string.nfc_not_available), Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}

	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Time time = new Time();
		time.setToNow();
		String text = BluetoothAdapter.getDefaultAdapter().getAddress();
		NdefMessage msg = new NdefMessage(
				NdefRecord
						.createMime(
								NFC_MIME_TYPE,
								text.getBytes()));
		return msg;
	}


	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	public void triggerSendMessage() {

		if (textToSend != null) {
			sendMessage(textToSend);
			setResult(RESULT_OK);
			finish();
		}
	}

	public void triggerReadMessage(String msg) {

		Intent i = new Intent();
		i.putExtra(READ_TEXT_ID, msg);
		setResult(RESULT_OK, i);
		finish();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

		} else {
			if (bluetoothCommunicationService == null) {
				// setupChat();
				bluetoothCommunicationService = new BluetoothCommunicationService(
						this, mHandler);
			}
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			// Activity started via NFC

			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);

			NdefMessage msg = (NdefMessage) rawMsgs[0];
			if (D)
				Log.d(TAG,
						"Data obtained by NFC: "
								+ new String(msg.getRecords()[0].getPayload()));
			remoteMACAddress = new String(msg.getRecords()[0].getPayload());

		} else if (getIntent().getExtras().containsKey(CALLING_INTENT_ID)) {

			// Called by ExtApp
			Intent i = (Intent) getIntent().getExtras().get(CALLING_INTENT_ID);
			Parcelable[] rawMsgs = i
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

			NdefMessage msg = (NdefMessage) rawMsgs[0];
			if (D)
				Log.d(TAG,
						"Data obtained by NFC: "
								+ new String(msg.getRecords()[0].getPayload()));
			remoteMACAddress = new String(msg.getRecords()[0].getPayload());

		} else {
			// Activity started manually
			beamMACAddress();

			textToSend = getIntent().getExtras().getString(DATA_TO_SEND_ID);
			if (D)
				Log.d(TAG, "Set textToSend: " + textToSend);

		}

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (bluetoothCommunicationService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (bluetoothCommunicationService.getState() == BluetoothCommunicationService.STATE_NONE) {
				// Start the Bluetooth communication services
				bluetoothCommunicationService.start();
			}

			// connect to device if MAC address is available
			if (remoteMACAddress != null) {
				connectDirectSecure();
			}

		}
	}

	private void connectDirectSecure() {

		if (D)
			Log.d(TAG, "Connecting to device with MAC: " + remoteMACAddress);
		BluetoothDevice device = bluetoothAdapter
				.getRemoteDevice(remoteMACAddress);

		bluetoothCommunicationService.connect(device, true);
	}


	@Override
	public synchronized void onPause() {
		super.onPause();
		if (bluetoothCommunicationService != null)
			bluetoothCommunicationService.stop();
		if (D)
			Log.e(TAG, "- ON PAUSE -");

	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void sendMessage(String message) {

		if (bluetoothCommunicationService.getState() != BluetoothCommunicationService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (message.length() > 0) {

			byte[] send = message.getBytes();
			bluetoothCommunicationService.write(send);
		}
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothCommunicationService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							connectedDeviceName));
					triggerSendMessage();
					break;
				case BluetoothCommunicationService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothCommunicationService.STATE_LISTEN:
				case BluetoothCommunicationService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				// Do nothing here so far.
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);
				triggerReadMessage(readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				connectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						getString(R.string.connected_to) + connectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_ABORT:
				bluetoothCommunicationService.stop();
				finish();
				break;
			}

		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {

				bluetoothCommunicationService = new BluetoothCommunicationService(
						this, mHandler);
			} else {

				if(D) Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// we do not use the menu option
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// we do not use the menu option
		return false;
	}

}

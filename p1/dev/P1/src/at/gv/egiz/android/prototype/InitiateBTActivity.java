package at.gv.egiz.android.prototype;

import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import at.gv.egiz.android.R;
import at.gv.egiz.android.application.LocationProverApplication;
import at.gv.egiz.android.communication.P2PCommunicationDevice;
import at.gv.egiz.android.communication.bluetooth.BluetoothCommunicationDevice;
import at.gv.egiz.android.communication.bluetooth.BluetoothConnectionManager;
import at.gv.egiz.android.communication.bluetooth.BluetoothConstants;
import at.gv.egiz.android.communication.bluetooth.BluetoothUtils;
import at.gv.egiz.android.communication.exception.InvalidRemoteAddressException;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.nfc.NFCUtils;
import at.gv.egiz.android.signature.SignatureCreationConstants;
import at.gv.egiz.android.storage.SDCardAdapter;
import at.gv.egiz.android.storage.StorageAdapter;
import at.gv.egiz.android.tltt.ShowTicketActivity;
import at.gv.egiz.android.tltt.constants.TLTT;
import at.gv.egiz.android.tltt.utils.FileUtils;

public class InitiateBTActivity extends Activity {

	private String connectedDeviceName = null;

	private String tag = DebugTags.EGIZ + " " + DebugTags.NFC
			+ " InitiateBTActivity";

	private P2PCommunicationDevice btDevice = null;
	private String remoteMacAddress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "NFC on Create()...");

		setContentView(R.layout.initiate_bt);

		// if Activity has already been created before, and if Bluetooth has
		// already been initialized, reuse the old BluetoothCommunicationDevice
		LocationProverApplication globalState = ((LocationProverApplication) getApplicationContext());
		if (globalState.getCommunicationDevice() != null
				&& globalState.getRemoteMacAddress() != null) {
			Log.d(tag, "restore from global state...");
			btDevice = globalState.getCommunicationDevice();
			remoteMacAddress = globalState.getRemoteMacAddress();
		} else {
			Intent startingIntent = this.getIntent();
			if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(startingIntent
					.getAction())) {
				finish();
				return;
			}
			Log.d(tag, "Activity has been started with nfc contact...");

			String msg = NFCUtils.getNdefMessagesAsString(startingIntent);
			remoteMacAddress = NFCUtils.removeNDEFDelimiters(msg);
			Log.d(tag, "Got new NDEF message: " + msg
					+ ". Remote Mac address is: " + remoteMacAddress);

			btDevice = new BluetoothCommunicationDevice(
					InitiateBTActivity.this, handler);
			globalState.setCommunicationDevice(btDevice);
			globalState.setRemoteMacAddress(remoteMacAddress);

		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(tag, "onStart()");
		init();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(tag, "NFC on Pause()");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.quit_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.quit) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else
			return super.onOptionsItemSelected(item);

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case BluetoothConstants.REQUEST_ENABLE_BT:
			Log.d(tag, "Returning from settings menu to enable bluetooth...");
			init();
			break;

		case TLTT.SHOW_TICKET:
			if (resultCode == RESULT_OK && intent.hasExtra(TLTT.COMMAND)) {
				switch (intent.getExtras().getInt(TLTT.COMMAND)) {
				case TLTT.COMMAND_SIGN: // STEP 3: sign ticket
					P2PLocationProver.startSignatureCreation(
							InitiateBTActivity.this, intent.getExtras()
									.getString(TLTT.LOCATION_TIME_TICKET),
							false, false, null);
					break;
				case TLTT.COMMAND_SHARE: // STEP 5: share ticket
					Log.d(tag,
							"send back ticket: "
									+ intent.getExtras().getString(
											TLTT.LOCATION_TIME_TICKET));
					sendBTMessage(intent.getExtras().getString(
							TLTT.LOCATION_TIME_TICKET));
					TextView tv1 = (TextView) findViewById(R.id.textView1);
					tv1.setText("Ticket wird gesendet.");
					TextView tv2 = (TextView) findViewById(R.id.textView2);
					tv2.setText("");
					break;
				default:

				}
			}
		case SignatureCreationConstants.CREATE_SIGNATURE:
			Log.d(tag, "onResult: Create_signature");
			if (resultCode == RESULT_OK
					&& intent.hasExtra(SignatureCreationConstants.SIGNATURE)) {

				Log.d(tag, "Store retrieved signature in file.");
				String signature = intent.getExtras().getString(
						SignatureCreationConstants.SIGNATURE);
				if (signature != null) {
					Log.d(tag, signature);
					StorageAdapter storage = new SDCardAdapter(
							getApplicationContext());

					storage.write(signature, "/locationTimeTickets/",
							FileUtils.getTimestamp() + ".xml");

					// display signed ticket
					Intent intentShow = new Intent(InitiateBTActivity.this,
							ShowTicketActivity.class);
					intentShow.putExtra(TLTT.LOCATION_TIME_TICKET, signature);
					intentShow.putExtra(TLTT.ACTIVITY_MENU_ID,
							R.menu.share_ticket_menu);
					startActivityForResult(intentShow, TLTT.SHOW_TICKET);

				}// TODO else error

			} else if (resultCode == RESULT_CANCELED) {
				if (intent.hasExtra(SignatureCreationConstants.ERROR_BOX)) {
					Log.d(tag, "Error occured. Start creation again.");

					P2PLocationProver
							.startSignatureCreation(
									InitiateBTActivity.this,
									intent.getStringExtra(SignatureCreationConstants.TEXT_TO_SIGN),
									intent.getBooleanExtra(
											SignatureCreationConstants.CAPTURE_TAN,
											Boolean.FALSE),
									intent.getBooleanExtra(
											SignatureCreationConstants.USE_TEST_SIGNATURE,
											Boolean.FALSE),
									intent.getStringExtra(SignatureCreationConstants.ERROR_BOX));
				} else {
					Log.d(tag, "quitting the application...");
					finish();
				}
			} else {

			}
		}
	}

	public void sendBTMessage(String message) {
		if (btDevice != null) {
			try {
				btDevice.send(BluetoothUtils.computeSha2Hash(message) + "::::"
						+ message);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			Log.d(tag, "Bluetooth Device is null.");
	}

	public void stopBTCommunication(View view) {
		stop();
	}

	private void stop() {
		if (btDevice != null) {
			btDevice.destroyConnection();
		}
	}

	private void init() {

		// check if Bluetooth is available on the device, enable it if it is
		// disabled
		int btStatus = BluetoothUtils.getBTStatus(this);

		// If the adapter is null, then Bluetooth is not supported
		if (btStatus == BluetoothConstants.BT_NOT_SUPPORTED) {
			Log.d(tag, "Bluetooth is not supported by this device. Exit now...");
			finish();
			return;
		}
		// Bluetooth is supported by the device, but not active, user needs to
		// enable it
		if (btStatus == BluetoothConstants.BT_NOT_ENABLED) {
			Log.d(tag, "Bluetooth not yet enabled.");
			BluetoothUtils.showBTSettings(this);

		} else {
			Log.d(tag, "Bluetooth is enabled.");
			if (btDevice.isDeviceReady())
				btDevice.setUpDevice();
			try {
				if (!btDevice.isConnected())
					btDevice.connect(remoteMacAddress,
							BluetoothConstants.SECURE_COMMUNICATION);
			} catch (InvalidRemoteAddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				stop();
			}
			// TODO what to do if not yet ready???? loop, wait some time,
			// timeout, exit
		}

	}

	private final Handler handler = new Handler() {
		protected String tempMessage = "";

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothConstants.MESSAGE_STATE_CHANGE:
				Log.i(tag, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothConnectionManager.STATE_CONNECTED:
				case BluetoothConnectionManager.STATE_CONNECTING:
				case BluetoothConnectionManager.STATE_LISTEN:
				case BluetoothConnectionManager.STATE_NONE:
					break;
				}
				break;
			case BluetoothConstants.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// Log.d(tag, "Send message: " + writeMessage);
				break;
			case BluetoothConstants.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				boolean end = false;
				if (readMessage.endsWith("<<</COMMAND>>>")) {
					end = true;
				}
				tempMessage += readMessage;

				if (end) {
					tempMessage = tempMessage.replace("<<<COMMAND>>>", "");
					tempMessage = tempMessage.replace("<<</COMMAND>>>", "");
					readMessage = tempMessage;
					tempMessage = "";
					Log.d(tag, "Read message: " + readMessage);
					String hashSent = BluetoothUtils.extractHash(readMessage);
					readMessage = BluetoothUtils.removeHash(readMessage);
					Log.d(tag, "The sent hash: " + hashSent);
					String hashNow = null;
					try {
						hashNow = BluetoothUtils.computeSha2Hash(readMessage);
					} catch (NoSuchAlgorithmException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					Log.d(tag, "Now created hash: " + hashNow);

					if (hashNow.equals(hashSent)) {
						Log.d(tag,
								"Start ShowTicketActivity with newly received ticket.");

						// display signed ticket
						Intent intentShow = new Intent(InitiateBTActivity.this,
								ShowTicketActivity.class);
						intentShow.putExtra(TLTT.LOCATION_TIME_TICKET,
								readMessage);
						intentShow.putExtra(TLTT.ACTIVITY_MENU_ID,
								R.menu.sign_ticket_menu);
						startActivityForResult(intentShow, TLTT.SHOW_TICKET);
					} else {
						Log.d(tag, "Hash does not match.");
						Toast.makeText(InitiateBTActivity.this,
								"TLTT corrupted.", Toast.LENGTH_LONG).show();
					}
				}

				break;
			case BluetoothConstants.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				connectedDeviceName = msg.getData().getString(
						BluetoothConstants.DEVICE_NAME);
				Log.d(tag, "Successfully connected to " + connectedDeviceName);
				Toast.makeText(InitiateBTActivity.this,
						"Connected to " + connectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothConstants.MESSAGE_TOAST:
				Toast.makeText(InitiateBTActivity.this,
						msg.getData().getString(BluetoothConstants.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothConstants.MESSAGE_CONNECTION_LOST:
				Log.d(tag, "Obtained CONNECTION LOST message.");
				Toast.makeText(InitiateBTActivity.this,
						msg.getData().getString(BluetoothConstants.TOAST),
						Toast.LENGTH_SHORT).show();
				Log.d(tag, "Connection got lost - quit App..");
				break;
			}
		}
	};
}

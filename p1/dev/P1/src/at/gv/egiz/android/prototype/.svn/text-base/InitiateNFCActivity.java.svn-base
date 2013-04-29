package at.gv.egiz.android.prototype;

import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;
import at.gv.egiz.android.R;
import at.gv.egiz.android.application.LocationProverApplication;
import at.gv.egiz.android.communication.P2PCommunicationDevice;
import at.gv.egiz.android.communication.bluetooth.BluetoothCommunicationDevice;
import at.gv.egiz.android.communication.bluetooth.BluetoothConnectionManager;
import at.gv.egiz.android.communication.bluetooth.BluetoothConstants;
import at.gv.egiz.android.communication.bluetooth.BluetoothUtils;
import at.gv.egiz.android.debug.DebugTags;
import at.gv.egiz.android.nfc.NFCConstants;
import at.gv.egiz.android.nfc.NFCUtils;
import at.gv.egiz.android.tltt.ShowTicketActivity;
import at.gv.egiz.android.tltt.constants.TLTT;

public class InitiateNFCActivity extends Activity {

	private String connectedDeviceName = null;

	private String tag = DebugTags.EGIZ + " " + DebugTags.NFC
			+ " InitiateNFCActivity";
	private NfcAdapter mNfcAdapter = null;

	private P2PCommunicationDevice btDevice = null;
	private String contentToSend = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "on Create()");

		setContentView(R.layout.initiate_nfc);

		// if Activity has already been created before, and if Bluetooth has
		// already been initialized, reuse the old BluetoothCommunicationDevice
		LocationProverApplication globalState = ((LocationProverApplication) getApplicationContext());
		if (globalState.getCommunicationDevice() != null) {
			Log.d(tag, "restore from global state...");
			btDevice = globalState.getCommunicationDevice();
		} else {
			btDevice = new BluetoothCommunicationDevice(
					InitiateNFCActivity.this, handler);
			globalState.setCommunicationDevice(btDevice);
		}

		Intent startingIntent = this.getIntent();
		if (startingIntent.hasExtra(BluetoothConstants.BT_MESSAGE)) {
			contentToSend = startingIntent
					.getStringExtra(BluetoothConstants.BT_MESSAGE);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(tag, "onStart()");

		init();

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(tag, "onResume()");

		/** push the msg to the other device, if one is in proximity **/
		if (mNfcAdapter != null) {
			Log.d(tag,
					"Send NDEF message with local MAC: "
							+ btDevice.getLocalMacAddress()
							+ " if other NFC device is in proximity.");
			// API Level 10
			// mNfcAdapter.enableForegroundNdefPush(
			// DummyInitiateNFCActivity.this,
			// NFCUtils.createNdefMessage(Locale.ENGLISH,
			// btDevice.getLocalMacAddress(), true));
			// API Level 14
			mNfcAdapter.setNdefPushMessage(
					NFCUtils.createNdefMessage(Locale.ENGLISH,
							btDevice.getLocalMacAddress(), true), this);
		}
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

	private void init() {
		int nfcStatus = NFCUtils.getNFCStatus(this);
		if (nfcStatus == NFCConstants.NFC_ENABLED) {
			mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			// in case NFC is not enabled there is no need to set up Bluetooth
			Log.d(tag, "btChannel is ready: " + btDevice.isDeviceReady());
			Log.d(tag, "btChannel is connected: " + btDevice.isConnected());
			if (btDevice.isDeviceReady() && !btDevice.isConnected()) {
				btDevice.setUpDevice();
			}
		} else if (nfcStatus == NFCConstants.NFC_NOT_ENABLED) {
			promptUserToEnableNFC();
		} else {
			Log.d(tag,
					"Near Field Communication is not supported by this device. Exit now...");
			finish();
			return;
		}

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
			// TODO what to do if not yet ready???? loop, wait some time,
			// timeout, exit
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(tag, "onPause()");
		// API Level 10
		// if (mNfcAdapter != null)
		// mNfcAdapter
		// .disableForegroundNdefPush(DummyInitiateNFCActivity.this);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case BluetoothConstants.REQUEST_ENABLE_BT:
			Log.d(tag, "Returning from settings menu to enable bluetooth...");
			init();
			break;
		case NFCConstants.REQUEST_ENABLE_NFC:
			Log.d(tag, "Returning from settings menu to enable NFC...");
			init();
			break;
		case TLTT.SHOW_TICKET:
			if (resultCode == RESULT_OK && intent.hasExtra(TLTT.COMMAND)) {
				switch (intent.getExtras().getInt(TLTT.COMMAND)) {
				case TLTT.COMMAND_SIGN: // STEP 3: sign ticket
					// startSignatureCreation(
					// intent.getExtras().getString(
					// TLTT.LOCATION_TIME_TICKET), false, false);
					break;
				case TLTT.COMMAND_SHARE: // STEP 5: share ticket

					break;
				default:

				}
			}

		}
	}

	public void sendBTMessage() {
		if (btDevice != null) {
			try {
				btDevice.send(BluetoothUtils.computeSha2Hash(contentToSend)
						+ "::::" + contentToSend);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			Log.d(tag, "Bluetooth Device is null.");
	}

	private void promptUserToEnableNFC() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Application needs NFC. Enable it now or exit. If you want to be able to initiate a connection, make sure Android Beam is enabled too.")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								NFCUtils.showNFCSettings(InitiateNFCActivity.this);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						InitiateNFCActivity.this.finish();
					}
				}).show();

	}

	public void stopBTCommunication(View view) {
		stop();
	}

	private void stop() {
		if (btDevice != null) {
			btDevice.destroyConnection();
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
					Log.i(tag, "Devices are connected. Send Message now.");
					sendBTMessage();
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
						Log.d(tag,
								"The hash: "
										+ BluetoothUtils
												.extractHash(readMessage));
						readMessage = BluetoothUtils.removeHash(readMessage);
						// display signed ticket
						Intent intentShow = new Intent(
								InitiateNFCActivity.this,
								ShowTicketActivity.class);
						intentShow.putExtra(TLTT.LOCATION_TIME_TICKET,
								readMessage);
						intentShow.putExtra(TLTT.ACTIVITY_MENU_ID,
								R.menu.sign_ticket_menu);
						startActivityForResult(intentShow, TLTT.SHOW_TICKET);
					} else {
						Log.d(tag, "Hash does not match.");
						Toast.makeText(InitiateNFCActivity.this,
								"TLTT corrupted.", Toast.LENGTH_LONG).show();
					}
				}

				break;
			case BluetoothConstants.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				connectedDeviceName = msg.getData().getString(
						BluetoothConstants.DEVICE_NAME);
				Log.d(tag, "Successfully connected to " + connectedDeviceName);
				Toast.makeText(InitiateNFCActivity.this,
						"Connected to " + connectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothConstants.MESSAGE_TOAST:
				Toast.makeText(InitiateNFCActivity.this,
						msg.getData().getString(BluetoothConstants.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothConstants.MESSAGE_CONNECTION_LOST:
				Log.d(tag, "Obtained CONNECTION LOST message.");
				Toast.makeText(InitiateNFCActivity.this,
						msg.getData().getString(BluetoothConstants.TOAST),
						Toast.LENGTH_SHORT).show();
				Log.d(tag, "Connection got lost - quit App..");
				break;
			}
		}
	};

}

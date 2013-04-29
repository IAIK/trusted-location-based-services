package at.gv.egiz.android.communication.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import at.gv.egiz.android.communication.P2PCommunicationDevice;
import at.gv.egiz.android.communication.exception.InvalidRemoteAddressException;
import at.gv.egiz.android.debug.DebugTags;

public class BluetoothCommunicationDevice implements P2PCommunicationDevice {

	private static final String TAG = DebugTags.EGIZ + DebugTags.BLUETOOTH
			+ "::BluetoothCommunicationDevice";

	private BluetoothConnectionManager connectionManager = null;

	protected Activity activity = null;
	private Handler messageHandler = null;

	public BluetoothCommunicationDevice(Activity activity,
			Handler messageHandler) {
		this.activity = activity;
		this.messageHandler = messageHandler;
	}

	@Override
	/**
	 * Start listening for incoming connections.
	 */
	public void setUpDevice() {
		if (connectionManager == null) {
			Log.d(TAG,
					"BluetoothConnectionManager is not yet initialized. Do it now...");
			// start the accepting Threads for setting up the Bluetooth
			// connection
			if (activity != null && messageHandler != null) {
				connectionManager = new BluetoothConnectionManager(activity,
						handler);
				connectionManager.start();
			} else {
				Log.d(TAG,
						"Error: Initialization was not done properly. Either handler or starting Activity have been null.");
			}
		} else {
			Log.d(TAG,
					"BluetoothConnectionManager has already been set up before, don't start up again. Is in state: "
							+ connectionManager.getState());
		}

	}

	@Override
	public boolean isDeviceReady() {
		if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON)
			return true;
		else
			return false;
	}

	@Override
	public String getLocalMacAddress() {
		return BluetoothAdapter.getDefaultAdapter().getAddress();
	}

	@Override
	public void connect(String remote_mac_address, boolean secureConnection)
			throws InvalidRemoteAddressException {
		if (connectionManager != null) {

			// stop the not needed accept thread
			if (secureConnection) {
				connectionManager.stopInsecureAccept();
			} else {
				connectionManager.stopSecureAccept();
			}
			try {
				BluetoothDevice remoteDevice = BluetoothAdapter
						.getDefaultAdapter()
						.getRemoteDevice(remote_mac_address);
				connectionManager.connect(remoteDevice, secureConnection);

			} catch (IllegalArgumentException e) {
				throw new InvalidRemoteAddressException(
						"Given remote Mac Address is not a valid Mac Address.");
			}
		} else {
			Log.d(TAG, "BluetoothConnectionManager is null.");
		}
	}

	@Override
	public void send(String message) {
		if (connectionManager == null) {
			Log.d(TAG, "BluetoothConnectionManager is null.");
			return;
		}
		if (connectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED) {
			Log.d(TAG, "devices are connected. send bluetooth message.");

			if (message.length() > 0) {
				String messageToBeSent = BluetoothConstants.BT_DELIMITER_FRONT
						+ message + BluetoothConstants.BT_DELIMITER_BACK;

				boolean finished = false;
				while (!finished) {
					int bufferLength = messageToBeSent.length();
					if (bufferLength > 4096) {
						bufferLength = 4096;
					} else {
						finished = true;
					}
					byte[] send = messageToBeSent.substring(0, bufferLength)
							.getBytes();
					connectionManager.write(send);
					// try {
					// Thread.sleep(600); // TODO why wait?
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					if (!finished) {
						messageToBeSent = messageToBeSent
								.substring(bufferLength);
					}
				}
			}
		} else {
			// devices are not connected yet
			Log.d(TAG, "devices are not connected yet.");
		}

	}

	@Override
	public boolean isConnected() {
		if (connectionManager == null)
			return false;
		if (connectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void destroyConnection() {
		if (connectionManager != null) { // may be the case if other user has
											// already closed connection
			connectionManager.stop();
			connectionManager = null;
		}
	}

	private final Handler handler = new Handler() {

		// forward the retrieved messages to the handler in the calling
		// activity, only in certain cases apply own
		// action e.g. MESSAGE_CONNECTION_LOST
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case BluetoothConstants.MESSAGE_STATE_CHANGE:
			case BluetoothConstants.MESSAGE_WRITE:
			case BluetoothConstants.MESSAGE_READ:
				messageHandler.obtainMessage(msg.what, msg.arg1, msg.arg2,
						msg.obj).sendToTarget();
				break;

			case BluetoothConstants.MESSAGE_DEVICE_NAME:
			case BluetoothConstants.MESSAGE_TOAST:
				Message newMsg = new Message(); // create new message, because
												// otherwise RuntimeException -
												// Message is already in use
				newMsg.setData(msg.getData());
				messageHandler.sendMessage(newMsg);
				break;
			case BluetoothConstants.MESSAGE_CONNECTION_LOST:
				// the other app has stopped the connection, stop the manager
				if (connectionManager != null) {
					connectionManager.stop();
					connectionManager = null;
				}
				Message newMsg1 = new Message();// create new message, because
												// otherwise RuntimeException -
												// Message is already in use
				newMsg1.setData(msg.getData());
				messageHandler.sendMessage(newMsg1);
				break;
			}

		}
	};

}

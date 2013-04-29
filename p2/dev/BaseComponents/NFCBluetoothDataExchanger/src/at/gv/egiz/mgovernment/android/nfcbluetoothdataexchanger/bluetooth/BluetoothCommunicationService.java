package at.gv.egiz.mgovernment.android.nfcbluetoothdataexchanger.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author Thomas Zefferer
 * Code adapted from Android sample project
 * 
 */

public class BluetoothCommunicationService {
	// Debugging
	private static final String TAG = "BluetoothCommunicationService";
	private static final boolean D = false;

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "BluetoothCommunicationSecure";
	private static final String NAME_INSECURE = "BluetoothCommunicationInsecure";

	// Unique UUID for this application
	private static final UUID MY_UUID_SECURE = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a89");
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("8ce255c0-200a-11e0-ac64-0800200c9a89");

	private final BluetoothAdapter adapter;
	private final Handler activityHandler;
	private AcceptThread secureAcceptThread;
	private AcceptThread insecureAcceptThread;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private int communicationState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	public static final String SOCKET_TYPE_SECURE = "Secure";
	public static final String SOCKET_TYPE_INSECURE = "Insecure";

	public BluetoothCommunicationService(Context context, Handler handler) {
		adapter = BluetoothAdapter.getDefaultAdapter();
		communicationState = STATE_NONE;
		activityHandler = handler;
	}

	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + communicationState + " -> " + state);
		communicationState = state;

		// Send the new state to the Handler so the calling Activity can update
		// its UI
		activityHandler.obtainMessage(
				BluetoothDataExchanger.MESSAGE_STATE_CHANGE, state, -1)
				.sendToTarget();
	}

	public synchronized int getState() {
		return communicationState;
	}

	public synchronized void start() {
		if (D)
			Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		setState(STATE_LISTEN);

		// Start the thread to listen on a BluetoothServerSocket
		if (secureAcceptThread == null) {
			secureAcceptThread = new AcceptThread(true);
			secureAcceptThread.start();
		}
		if (insecureAcceptThread == null) {
			insecureAcceptThread = new AcceptThread(false);
			insecureAcceptThread.start();
		}
	}

	public synchronized void connect(BluetoothDevice device, boolean secure) {
		if (D)
			Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (communicationState == STATE_CONNECTING) {
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		// Start the thread to connect with the given device
		connectThread = new ConnectThread(device, secure);
		connectThread.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device, final String socketType) {

		if (D)
			Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (secureAcceptThread != null) {
			secureAcceptThread.cancel();
			secureAcceptThread = null;
		}
		if (insecureAcceptThread != null) {
			insecureAcceptThread.cancel();
			insecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(socket, socketType);
		connectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = activityHandler
				.obtainMessage(BluetoothDataExchanger.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothDataExchanger.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		activityHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");

		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		if (secureAcceptThread != null) {
			secureAcceptThread.cancel();
			secureAcceptThread = null;
		}

		if (insecureAcceptThread != null) {
			insecureAcceptThread.cancel();
			insecureAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	public void write(byte[] out) {

		ConnectedThread r;

		synchronized (this) {
			if (communicationState != STATE_CONNECTED)
				return;
			r = connectedThread;
		}

		r.write(out);
	}

	private void connectionFailed() {

		Message msg = activityHandler
				.obtainMessage(BluetoothDataExchanger.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothDataExchanger.TOAST,
				"Unable to connect device");
		msg.setData(bundle);
		activityHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		BluetoothCommunicationService.this.start();
	}

	private void connectionLost() {

		// Send message to calling activity to cause it to finish
		Message msg = activityHandler
				.obtainMessage(BluetoothDataExchanger.MESSAGE_ABORT);
		activityHandler.sendMessage(msg);

		BluetoothCommunicationService.this.stop();
	}

	private class AcceptThread extends Thread {

		private final BluetoothServerSocket serverSocket;
		private String socketType;

		public AcceptThread(boolean secure) {
			BluetoothServerSocket tmp = null;
			socketType = secure ? "Secure" : "Insecure";

			try {
				if (secure) {
					tmp = adapter.listenUsingRfcommWithServiceRecord(
							NAME_SECURE, MY_UUID_SECURE);
				} else {
					tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(
							NAME_INSECURE, MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				if (D)
					Log.e(TAG,
							"Socket Type: " + socketType + "listen() failed", e);
			}
			serverSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "Socket Type: " + socketType + "BEGIN mAcceptThread"
						+ this);
			setName("AcceptThread" + socketType);

			BluetoothSocket socket = null;

			while (communicationState != STATE_CONNECTED) {
				try {

					socket = serverSocket.accept();
				} catch (IOException e) {
					if (D)
						Log.e(TAG, "Socket Type: " + socketType
								+ "accept() failed", e);
					break;
				}

				if (socket != null) {
					synchronized (BluetoothCommunicationService.this) {
						switch (communicationState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							connected(socket, socket.getRemoteDevice(),
									socketType);
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							try {
								socket.close();
							} catch (IOException e) {
								if (D)
									Log.e(TAG,
											"Could not close unwanted socket",
											e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread, socket Type: " + socketType);

		}

		public void cancel() {
			if (D)
				Log.d(TAG, "Socket Type" + socketType + "cancel " + this);
			try {
				serverSocket.close();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "Socket Type" + socketType
							+ "close() of server failed", e);
			}
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice bluetoothDevice;
		private String socketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			bluetoothDevice = device;
			BluetoothSocket tmp = null;
			socketType = secure ? SOCKET_TYPE_SECURE : SOCKET_TYPE_INSECURE;

			try {
				if (secure) {
					tmp = device
							.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
				} else {
					tmp = device
							.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				if (D)
					Log.e(TAG,
							"Socket Type: " + socketType + "create() failed", e);
			}
			socket = tmp;
		}

		public void run() {
			if (D)
				Log.i(TAG, "BEGIN mConnectThread SocketType:" + socketType);
			setName("ConnectThread" + socketType);

			// Always cancel discovery because it will slow down a connection
			adapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				socket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					socket.close();
				} catch (IOException e2) {
					if (D)
						Log.e(TAG, "unable to close() " + socketType
								+ " socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothCommunicationService.this) {
				connectThread = null;
			}

			// Start the connected thread
			connected(socket, bluetoothDevice, socketType);
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "close() of connect " + socketType
							+ " socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket socket;
		private final InputStream inStream;
		private final OutputStream outStream;

		public ConnectedThread(BluetoothSocket bluetoothSocket,
				String socketType) {
			if (D)
				Log.d(TAG, "create ConnectedThread: " + socketType);
			socket = bluetoothSocket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = bluetoothSocket.getInputStream();
				tmpOut = bluetoothSocket.getOutputStream();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "temp sockets not created", e);
			}

			inStream = tmpIn;
			outStream = tmpOut;
		}

		public void run() {
			if (D)
				Log.i(TAG, "BEGIN connectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			while (true) {
				try {

					bytes = inStream.read(buffer);

					activityHandler.obtainMessage(
							BluetoothDataExchanger.MESSAGE_READ, bytes, -1,
							buffer).sendToTarget();
				} catch (IOException e) {
					if (D)
						Log.e(TAG, "disconnected", e);
					connectionLost();
					// Start the service over to restart listening mode
					BluetoothCommunicationService.this.start();
					break;
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				outStream.write(buffer);
				activityHandler.obtainMessage(
						BluetoothDataExchanger.MESSAGE_WRITE, -1, -1, buffer)
						.sendToTarget();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.gv.egiz.android.communication.bluetooth;

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
import at.gv.egiz.android.debug.DebugTags;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class BluetoothConnectionManager {

	// Debugging
	private static final String TAG = DebugTags.EGIZ + DebugTags.BLUETOOTH
			+ "::BluetoothConnectionManager";

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "LocationSignerSecureBluetoothChannel";
	private static final String NAME_INSECURE = "LocationSignerInsecureBluetoothChannel";

	// Unique UUID for this application
	private static final UUID MY_UUID_SECURE = UUID
			.fromString("7a659570-0b81-11e1-be50-0800200c9a66");
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("ab1878e0-0b81-11e1-be50-0800200c9a66");

	// Member fields
	private final BluetoothAdapter bluetoothAdapter;
	private final Handler messageHandler;
	private AcceptThread secureAcceptThread;
	private AcceptThread insecureAcceptThread;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private int state;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothConnectionManager(Context context, Handler handler) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		state = STATE_NONE;
		messageHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param currentState
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int currentState) {
		Log.d(TAG, "setState() " + state + " -> " + currentState);
		state = currentState;

		// Give the new state to the Handler so the UI Activity can update
		messageHandler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE,
				currentState, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return state;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {

		Log.d(TAG, "BT Adapter state: " + bluetoothAdapter.getState());
		Log.d(TAG, "start up the accept threads.");

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

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice device, boolean secure) {
		Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (state == STATE_CONNECTING) {
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

		Log.d(TAG, "Start ConnectThread..");
		// Start the thread to connect with the given device
		connectThread = new ConnectThread(device, secure);
		connectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device, final String socketType) {
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
		Log.d(TAG,
				"Send the name of the connected device back to the UI Activity");
		Message msg = messageHandler
				.obtainMessage(BluetoothConstants.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothConstants.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		messageHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop secure accept thread
	 */
	public synchronized void stopSecureAccept() {
		Log.d(TAG, "stop secure accept");

		if (secureAcceptThread != null) {
			secureAcceptThread.cancel();
			secureAcceptThread = null;
		}
	}

	/**
	 * Stop insecure accept thread
	 */
	public synchronized void stopInsecureAccept() {
		Log.d(TAG, "stop insecure accept");

		if (insecureAcceptThread != null) {
			insecureAcceptThread.cancel();
			insecureAcceptThread = null;
		}
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
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

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (state != STATE_CONNECTED)
				return;
			r = connectedThread;
			r.write(out);
		}
		// Perform the write unsynchronized

	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		Message msg = messageHandler
				.obtainMessage(BluetoothConstants.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothConstants.TOAST, "Unable to connect device");
		msg.setData(bundle);
		messageHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		BluetoothConnectionManager.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		Message msg = messageHandler
				.obtainMessage(BluetoothConstants.MESSAGE_CONNECTION_LOST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothConstants.TOAST, "Device connection was lost");
		msg.setData(bundle);
		messageHandler.sendMessage(msg);

	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket bluetoothServerSocket;
		private String socketType;

		public AcceptThread(boolean secure) {
			BluetoothServerSocket tmp = null;
			socketType = secure ? "Secure" : "Insecure";

			// Create a new listening server socket
			try {
				if (secure) {
					tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
							NAME_SECURE, MY_UUID_SECURE);
				} else {
					tmp = bluetoothAdapter
							.listenUsingInsecureRfcommWithServiceRecord(
									NAME_INSECURE, MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + socketType + "listen() failed", e);
			}
			bluetoothServerSocket = tmp;
		}

		public void run() {
			Log.d(TAG, "Socket Type: " + socketType + "BEGIN AcceptThread"
					+ this);
			setName("AcceptThread" + socketType);

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (state != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = bluetoothServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG,
							"Socket Type: " + socketType + "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothConnectionManager.this) {
						switch (state) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice(),
									socketType);
							break; // DONNERSTAG, stop other accept thread
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			Log.i(TAG, "END AcceptThread, socket Type: " + socketType);

		}

		public void cancel() {
			Log.d(TAG, "Socket Type" + socketType + "cancel " + this);
			try {
				bluetoothServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Socket Type" + socketType
						+ "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket bluetoothSocket;
		private final BluetoothDevice bluetoothDevice;
		private String socketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			bluetoothDevice = device;
			BluetoothSocket tmp = null;
			socketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				if (secure) {
					tmp = device
							.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
				} else {
					tmp = device
							.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + socketType + "create() failed", e);
			}
			bluetoothSocket = tmp;
		}

		public void run() {
			Log.d(TAG, "BEGIN ConnectThread SocketType:" + socketType);
			setName("ConnectThread" + socketType);

			// Always cancel discovery because it will slow down a connection
			bluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				bluetoothSocket.connect();

			} catch (IOException e) {
				// Close the socket
				Log.d(TAG, "Error during connecting: " + e.getMessage(), e);
				try {
					bluetoothSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + socketType
							+ " socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothConnectionManager.this) {
				connectThread = null;
			}

			// Start the connected thread
			connected(bluetoothSocket, bluetoothDevice, socketType);
		}

		public void cancel() {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
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
		private final BluetoothSocket bluetoothSocket;
		private final InputStream inputStream;
		private final OutputStream outputStream;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			bluetoothSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			inputStream = tmpIn;
			outputStream = tmpOut;
			// todo
			// http://stackoverflow.com/questions/12354643/android-bluetoothsocket-outputstream-write-blocks-infinitely
			// http://www.androidpit.de/de/android/forum/thread/455502/Bluetooth-data-sometimes-corrupted
			// http://droid-copter.googlecode.com/svn-history/r84/trunk/quadcopter-android/src/com/quadcopter/background/hardware/BluetoothCommunication.java
			// http://stackoverflow.com/questions/11447269/unable-to-read-input-from-a-bluetoothsocket
		}

		public void run() {
			Log.i(TAG, "BEGIN ConnectedThread");
			byte[] buffer = new byte[8192];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = inputStream.read(buffer);

					// Send the obtained bytes to the UI Activity
					messageHandler.obtainMessage(
							BluetoothConstants.MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				outputStream.write(buffer);

				try {
					Thread.sleep(300); // TODO
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outputStream.flush();
				// Share the sent message back to the UI Activity
				messageHandler.obtainMessage(BluetoothConstants.MESSAGE_WRITE,
						-1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
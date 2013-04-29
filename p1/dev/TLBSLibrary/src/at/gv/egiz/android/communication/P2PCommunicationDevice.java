package at.gv.egiz.android.communication;

import at.gv.egiz.android.communication.exception.InvalidRemoteAddressException;

public interface P2PCommunicationDevice {

	void setUpDevice();

	/**
	 * Connects to the remote device.
	 * 
	 * @param remote_mac_address
	 *            - device which we want to connect with
	 * @param secureConnection
	 *            - enforces a secure connection
	 */
	void connect(String remote_mac_address, boolean secureConnection)
			throws InvalidRemoteAddressException;

	/**
	 * Send a message to the connected remote device.
	 * 
	 * @param message
	 */
	void send(String message);

	/**
	 * Returns the local address of the device.
	 * 
	 * @return the MAC Address of our local device.
	 */
	String getLocalMacAddress();

	/**
	 * Returns true if device is connected to the remote device.
	 * 
	 * @return
	 */
	boolean isConnected();

	/**
	 * Returns true if device is ready for operation.
	 * 
	 * @return
	 */
	boolean isDeviceReady();

	/**
	 * Clean up connections.
	 */
	void destroyConnection();

}

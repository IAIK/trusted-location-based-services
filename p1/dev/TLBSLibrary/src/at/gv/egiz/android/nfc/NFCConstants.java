package at.gv.egiz.android.nfc;

public class NFCConstants {

	// status codes
	/**
	 * NFC is not supported on this device.
	 */
	public static final int NFC_NOT_SUPPORTED = 9000;
	/**
	 * NFC is enabled on this device.
	 */
	public static final int NFC_ENABLED = 9001;
	/**
	 * NFC is not enabled on this device
	 */
	public static final int NFC_NOT_ENABLED = 9002;

	/**
	 * Mime Type used for NDEF messages.
	 */
	public static final String NFC_MIME_TYPE = "application/egiz-locationprover";

	// Activity request codes
	/**
	 * Request code for enabling NFC on the device
	 */
	public static final int REQUEST_ENABLE_NFC = 9100;

	// Delimiter for our custom NDEF messages
	public static final String NDEF_DELIMITER_FRONT = "<<<COMMAND>>>";
	public static final String NDEF_DELIMITER_BACK = "<<</COMMAND>>>";
}

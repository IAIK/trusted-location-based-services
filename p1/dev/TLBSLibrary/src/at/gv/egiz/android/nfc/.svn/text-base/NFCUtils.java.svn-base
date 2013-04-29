package at.gv.egiz.android.nfc;

import java.nio.charset.Charset;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import at.gv.egiz.android.debug.DebugTags;

/**
 * Helper methods for NFC Messages.
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class NFCUtils {

	private static final String tag = DebugTags.EGIZ + " " + DebugTags.NFC
			+ " NFCUtils";

	private static final String UTF8 = "UTF-8";
	private static final String UTF16 = "UTF-16";
	private static final String ASCII = "US-ASCII";

	/**
	 * Creates a NDEF Message from String.
	 * 
	 * @param locale
	 *            - the {@link java.util.Locale}
	 * @param <code>String</code> text - the message
	 * @param <code>boolean</code> encodeInUtf8 - <code>true</code> if message
	 *        should be encoded in UTF-8, otherwise it will be encoded in UTF-16
	 * @return the NDEF Message
	 */
	public static NdefMessage createNdefMessage(Locale locale, String text,
			boolean encodeInUtf8) {

		text = NFCConstants.NDEF_DELIMITER_FRONT + text
				+ NFCConstants.NDEF_DELIMITER_BACK;
		byte[] langBytes = locale.getLanguage()
				.getBytes(Charset.forName(ASCII));

		Charset utfEncoding = encodeInUtf8 ? Charset.forName(UTF8) : Charset
				.forName(UTF16);
		byte[] textBytes = text.getBytes(utfEncoding);

		int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		char status = (char) (utfBit + langBytes.length);

		byte[] data = new byte[1 + langBytes.length + textBytes.length];
		data[0] = (byte) status;
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
				textBytes.length);

		byte[] mimeBytes = NFCConstants.NFC_MIME_TYPE.getBytes(Charset
				.forName(UTF8));

		byte[] dataBytes = text.getBytes(Charset.forName(UTF8));
		byte[] id = new byte[0]; // We don’t use the id field
		return new NdefMessage(new NdefRecord[] { new NdefRecord(
				NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes) });

	}

	/**
	 * Returns array of NdefMessages contained in the intent.
	 * 
	 * @param intent
	 * @return <code>null</code> if Intent is unknown, otherwise an array of
	 *         NdefMessages
	 */
	private static NdefMessage[] getNdefMessages(Intent intent) {
		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		} else {
			Log.d(tag, "Unknown intent.");
			msgs = null;
		}
		if (msgs != null)
			Log.d(tag, "Found " + msgs.length + " NDEF messages!");
		return msgs;
	}

	/**
	 * Returns a String with the content of the NDEF messages contained in the
	 * Intent.
	 * 
	 * @param an
	 *            Intent
	 * 
	 * @return String with parsed NDEF message (without delimiter!), empty
	 *         String if Intent does not contain any NDEF messages or messages
	 *         cannot be read.
	 */
	public static String getNdefMessagesAsString(Intent intent) {
		NdefMessage[] msgs = getNdefMessages(intent);
		if (msgs == null)
			return "";
		String m = "";
		for (int i = 0; i < msgs.length; i++) {
			NdefMessage msg = msgs[i];
			for (int j = 0; j < msg.getRecords().length; j++) {
				m += new String(msg.getRecords()[j].getPayload());
			}
		}
		Log.d(tag, "Obtained NDEF message: " + m);
		return m;
	}

	/**
	 * Determines if the device supports Near Field Communication and whether it
	 * is currently enabled or not. Beware that since Android 4.0, Android Beam
	 * is supported. Android Beam is equal to the write NFC functionality. So if
	 * NFC is enabled on a device >=4.0 that does not imply that Android Beam is
	 * enabled too. If Android Beam is disabled, the device is not able to send
	 * NFC message to either a tag or another device. So if you want to do a NFC
	 * write inform the user that therefore Android Beam has to be enabled.
	 * 
	 * @param activity
	 *            - that calls this method
	 * @return {@link at.gv.egiz.android.nfc.NFCConstants #NFC_NOT_SUPPORTED} if
	 *         device does not have NFC support.
	 *         {@link at.gv.egiz.android.nfc.NFCConstants #NFC_NOT_ENABLED} if
	 *         NFC is not enabled by the user, otherwise
	 *         {@link at.gv.egiz.android.nfc.NFCConstants #NFC_ENABLED}
	 */
	public static int getNFCStatus(Activity activity) {
		NfcManager manager = (NfcManager) activity.getApplicationContext()
				.getSystemService(Context.NFC_SERVICE);
		NfcAdapter nfcAdapter = manager.getDefaultAdapter();

		if (nfcAdapter == null) {
			// NFC is not supported on this phone
			return NFCConstants.NFC_NOT_SUPPORTED;
		}
		if (nfcAdapter.isEnabled()) {
			// adapter exists and is enabled.
			return NFCConstants.NFC_ENABLED;
		} else {
			return NFCConstants.NFC_NOT_ENABLED;
		}

	}

	/**
	 * Redirects the user to the settings menu in order to enable Near Field
	 * Communication. Calling Activity has to add handling code to
	 * onActivityResult with RequestCode
	 * {@link at.gv.egiz.android.nfc.NFCConstants #REQUEST_ENABLE_NFC}
	 * 
	 * 
	 * @param Activity
	 *            that calls this method
	 */
	public static void showNFCSettings(Activity activity) {
		Intent enableIntent = new Intent(Settings.ACTION_SETTINGS);
		activity.startActivityForResult(enableIntent,
				NFCConstants.REQUEST_ENABLE_NFC);
	}

	/**
	 * Removes the custom NDEF Message Delimiters
	 * {@link at.gv.egiz.android.nfc.NFCConstants #NDEF_DELIMITER_FRONT} and
	 * {@link at.gv.egiz.android.nfc.NFCConstants #NDEF_DELIMITER_BACK}.
	 * 
	 * @param <code>String</code> received NDEF message
	 * @return <code>String</code> without delimiters.
	 */
	public static String removeNDEFDelimiters(String msg) {
		msg = msg.replace(NFCConstants.NDEF_DELIMITER_FRONT, "");
		msg = msg.replace(NFCConstants.NDEF_DELIMITER_BACK, "");
		return msg;
	}

}

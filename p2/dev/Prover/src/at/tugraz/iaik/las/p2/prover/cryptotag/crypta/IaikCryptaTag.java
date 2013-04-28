package at.tugraz.iaik.las.p2.prover.cryptotag.crypta;

import iaik.security.ecc.util.SignatureFormater;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.ProverApp;
import at.tugraz.iaik.las.p2.prover.cryptotag.ICryptoTag;
import at.tugraz.iaik.las.p2.common.TagCrypto;

/***
 * This class implements the communication with the IAIK "CryptaTag"
 * ("CRYptographic Protected TAg").
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class IaikCryptaTag implements ICryptoTag {

	private IsoDep currentIsoDep;

	private static byte[] SELECT_BY_ID = new byte[] { (byte) 0x00, (byte) 0xA4,
			(byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00 };
	private static byte[] READ_BINARY = new byte[] { (byte) 0x00, (byte) 0xB0,
			(byte) 0x00, (byte) 0x00, (byte) 0x00 };
	private static byte[] INTERNAL_AUTHENTICATE_ECDSA = new byte[] {
			(byte) 0x00, (byte) 0x88, (byte) 0x01, (byte) 0x00, (byte) 0x10,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x30 };

	private static final boolean DEBUG = true;
	private static final short FILEID_UID = (short) 0x0005;

	public IaikCryptaTag(Tag tag) throws Exception {
		this.currentIsoDep = IsoDep.get(tag);
		if (this.currentIsoDep == null) {
			throw new Exception("Tag does not support ISO-DEP.");
		}
		try {
			this.currentIsoDep.connect();
		} catch (IOException e) {
			throw new Exception("Could not connect to Tag.");
		}
	}

	public void disconnect() {
	}

	public byte[] getUid() {
		if (this.currentIsoDep == null) {
			Log.d(ProverApp.P, "No tag in proximity.");
			return null;
		}

		// init
		byte[] uid = null;

		// select file
		if (!selectFile(this.currentIsoDep, FILEID_UID)) {
			Log.d(ProverApp.P, "NFC: Read UID failed!\n");
			return null;
		}

		// read public key
		uid = readBinary(this.currentIsoDep, (short) 0x0000, (byte) 0x00);
		if (uid == null || uid.length != 6) {
			Log.d(ProverApp.P, "NFC: Read UID failed!\n");
			return null;
		}

		// combine UID File with actual UID bytes
		// 0x3F ... manufacturer byte
		// 0x08 ... AMS Chip type
		// 0x00 ... custom byte
		byte[] uid_final = new byte[] { (byte) 0x3F, (byte) 0x08, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		uid_final[3] = uid[0];
		uid_final[4] = uid[1];
		uid_final[5] = uid[2];
		uid_final[6] = uid[3];

		// print UID information
		Log.d(ProverApp.P, "NFC: UID (length=" + uid_final.length + "): "
				+ Utils.byteArrayToHexString(uid_final) + "\n");

		// end
		Log.d(ProverApp.P, "NFC: UID successfully read!\n");
		return uid_final;
	}
	private boolean selectFile(IsoDep isoDep, short ID) {
		// init
		byte[] response = null;

		// create command
		SELECT_BY_ID[5] = (byte) (ID >> 8);
		SELECT_BY_ID[6] = (byte) (ID);

		// send command
		try {
			if (DEBUG)
				Log.d(ProverApp.P,
						"NFC: Sending: "
								+ Utils.byteArrayToHexString(SELECT_BY_ID)
								+ "\n");
			response = isoDep.transceive(SELECT_BY_ID);
			if (DEBUG)
				Log.d(ProverApp.P,
						"NFC: Received: "
								+ Utils.byteArrayToHexString(response) + "\n");
		} catch (IOException e) {
			Log.d(ProverApp.P, "NFC: IOException caught during transceive(): "
					+ e.getMessage());
			return (false);
		}

		// evaluate result
		if (response.length != 2) {
			Log.d(ProverApp.P,
					"NFC: Received message of invalid length! Expected(2), Received("
							+ response.length + ").\n");
			return (false);
		} else {
			if (response[0] == (byte) 0x90 && response[1] == (byte) 0x00) {
				Log.d(ProverApp.P, "NFC: File selection successful!\n");
				return (true);
			} else if (response[0] == (byte) 0x6A && response[1] == (byte) 0x82) {
				Log.d(ProverApp.P, "NFC: File or application not found!\n");
				return (false);
			} else if (response[0] == (byte) 0x69 && response[1] == (byte) 0x82) {
				Log.d(ProverApp.P, "NFC: Security status not satisfied!\n");
				return (false);
			} else if (response[0] == (byte) 0x6D && response[1] == (byte) 0x00) {
				Log.d(ProverApp.P, "NFC: Command not supported or invalid!\n");
				return (false);
			} else {
				Log.d(ProverApp.P, "NFC: Unkown response!\n");
				return (false);
			}
		}
	}

	private byte[] readBinary(IsoDep isoDep, short offset, byte length) {
		// init
		byte[] response = null;

		// create command
		READ_BINARY[2] = (byte) (offset >> 8);
		READ_BINARY[3] = (byte) (offset);

		// send command
		try {
			if (DEBUG)
				Log.d(ProverApp.P,
						"NFC: Sending: "
								+ Utils.byteArrayToHexString(READ_BINARY)
								+ "\n");
			response = isoDep.transceive(READ_BINARY);
			if (DEBUG)
				Log.d(ProverApp.P,
						"NFC: Received: "
								+ Utils.byteArrayToHexString(response) + "\n");
		} catch (IOException e) {
			Log.d(ProverApp.P, "NFC: IOException caught during transceive(): "
					+ e.getMessage());
			return (null);
		}

		// evaluate result
		if (response == null || response.length == 0) {
			Log.d(ProverApp.P,
					"NFC: Received message of invalid length! Expected("
							+ (int) length + "), Received(0).\n");
			return (null);
		} else {
			if (response[response.length - 2] == (byte) 0x90
					&& response[response.length - 1] == (byte) 0x00) {
				Log.d(ProverApp.P, "NFC: Read binary successful!\n");
				return (Arrays.copyOfRange(response, 0, response.length - 2));
			} else if (response[response.length - 2] == (byte) 0x62
					&& response[response.length - 1] == (byte) 0x82) {
				Log.d(ProverApp.P,
						"NFC: End-of-file reached before sending Le bytes!\n");
				return (Arrays.copyOfRange(response, 0, response.length - 2));
			} else if (response[response.length - 2] == (byte) 0x69
					&& response[response.length - 1] == (byte) 0x82) {
				Log.d(ProverApp.P, "NFC: Security status prevents reading!\n");
				return (null);
			} else if (response[response.length - 2] == (byte) 0x6D
					&& response[response.length - 1] == (byte) 0x00) {
				Log.d(ProverApp.P, "NFC: Command not supported or invalid!\n");
				return (null);
			} else {
				Log.d(ProverApp.P, "NFC: Unkown response!\n");
				return (null);
			}
		}
	}

	@Override
	public byte[] sign(byte[] data) {
		// init
		byte[] response = null;

		// check input
		if (data == null) {
			Log.e(ProverApp.P, "NFC: Invalid input! Null value not accepted!");
			return null;
		}
		if (data.length != TagCrypto.CRYPTO_TAG_NONCE_LENGTH) {
			Log.e(ProverApp.P, "NFC: Invalid input! Length must be 16!\n");
			return null;
		}
		
		// reverse data as need for crypta tag
		byte[] data_reversed = new byte[data.length];
		for(int i = 0; i < 4; i++)
		{
			data_reversed[4 * i]   = data[4 * i + 3];
			data_reversed[4 * i + 1] = data[4 * i + 2];
			data_reversed[4 * i + 2] = data[4 * i + 1];
			data_reversed[4 * i + 3] = data[4 * i + 0];
		}

		// create command
		for (int i = 5; i < 21; i++)
			INTERNAL_AUTHENTICATE_ECDSA[i] = data_reversed[i - 5];

		// send command
		try {
			// if(DEBUG)
			// Logger.log("NFC: Sending: " +
			// Utils.byteArrayToHexString(INTERNAL_AUTHENTICATE_ECDSA) + "");
			response = this.currentIsoDep
					.transceive(INTERNAL_AUTHENTICATE_ECDSA);
			// if(DEBUG)
			// Logger.log("NFC: Received: " +
			// Utils.byteArrayToHexString(response) + "");
		} catch (IOException e) {
			Log.e(ProverApp.P, "NFC: IOException caught during transceive(): "
					+ e.getMessage());
			return null;
		}

		// evaluate result
		if (response == null || response.length == 0) {
			Log.e(ProverApp.P,
					"NFC: Received message of invalid length! Expected(50 or 2), Received(0).");
			return null;
		} else if (response.length == 50) {
			if (response[response.length - 2] == (byte) 0x90
					&& response[response.length - 1] == (byte) 0x00) {
				Log.d(ProverApp.P, "NFC: Read binary successful!");
				return this.reorderData(Arrays.copyOfRange(response, 0, response.length - 2));
			} else {
				Log.e(ProverApp.P, "NFC: Unkown response!");
				return null;
			}
		} else if (response.length == 2) {
			if (response[response.length - 2] == (byte) 0x62
					&& response[response.length - 1] == (byte) 0x82) {
				Log.e(ProverApp.P,
						"NFC: End-of-file reached before sending Le bytes!");
				return null;
			} else if (response[response.length - 2] == (byte) 0x69
					&& response[response.length - 1] == (byte) 0x82) {
				Log.e(ProverApp.P, "NFC: Security status prevents reading!");
				return null;
			} else if (response[response.length - 2] == (byte) 0x6D
					&& response[response.length - 1] == (byte) 0x00) {
				Log.e(ProverApp.P, "NFC: Command not supported or invalid!");
				return null;
			} else {
				Log.e(ProverApp.P, "NFC: Unkown response!");
				return null;
			}
		} else {
			Log.e(ProverApp.P, "NFC: Unkown response!\n");
			return null;
		}
	}

	private byte[] reorderData(byte[] signature) {
		// get R, S
		byte[] r_reversed = Arrays.copyOfRange(signature, 0, 24);
		byte[] r_correct = new byte[24];
		for (int j = 0; j < 24; j++)
			r_correct[j] = r_reversed[23 - j];
		byte[] s_reversed = Arrays.copyOfRange(signature, 24, 48);
		byte[] s_correct = new byte[24];
		for (int j = 0; j < 24; j++)
			s_correct[j] = s_reversed[23 - j];

		// convert R,S into verifiable signature
		byte[] real_signature = SignatureFormater.getSignature(new BigInteger(
				1, r_correct), new BigInteger(1, s_correct));
		return real_signature;
	}
}

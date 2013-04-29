package at.gv.egiz.android.communication.bluetooth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class BluetoothUtils {

	/**
	 * Queries the user to enable Bluetooth on her device. Calling Activity has
	 * to add handling code to onActivityResult with RequestCode
	 * {@link at.gv.egiz.android.bluetooth.BluetoothConstants #REQUEST_ENABLE_BT}
	 * 
	 * 
	 * @param Activity
	 *            that calls this method
	 */
	public static void showBTSettings(Activity activity) {
		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableIntent,
				BluetoothConstants.REQUEST_ENABLE_BT);
	}

	/**
	 * Determines if the device supports Bluetooth and whether it is currently
	 * enabled or not.
	 * 
	 * @param activity
	 *            - that calls this method
	 * @return {@link at.gv.egiz.android.bluetooth.BluetoothConstants #BT_NOT_SUPPORTED}
	 *         if device does not have NFC support.
	 *         {@link at.gv.egiz.android.bluetooth.BluetoothConstants #BT_NOT_ENABLED}
	 *         if NFC is not enabled by the user, otherwise
	 *         {@link at.gv.egiz.android.bluetooth.BluetoothConstants #BT_ENABLED}
	 */
	public static int getBTStatus(Activity activity) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothAdapter == null) {
			// NFC is not supported on this phone
			return BluetoothConstants.BT_NOT_SUPPORTED;
		}
		if (bluetoothAdapter.isEnabled()) {
			// adapter exists and is enabled.
			return BluetoothConstants.BT_ENABLED;
		} else {
			return BluetoothConstants.BT_NOT_ENABLED;
		}

	}

	public static String computeSha2Hash(String data)
			throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(data.getBytes());

		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		System.out.println("Hex format : " + sb.toString());
		return sb.toString();
	}

	public static String extractHash(String data) {
		int i = data.indexOf("::::");
		String hash = data.substring(0, i + "::::".length());
		hash = hash.replace(":", "");
		return hash;
	}

	public static String removeHash(String data) {
		int i = data.indexOf("::::");
		String withoutHash = data.substring(i + "....".length());
		return withoutHash;
	}
}

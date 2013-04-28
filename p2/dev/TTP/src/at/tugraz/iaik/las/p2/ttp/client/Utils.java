package at.tugraz.iaik.las.p2.ttp.client;

/***
 * Support functions for byte array handling.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public final class Utils {

	/***
	 * Converts a byte array to a hex string.
	 * 
	 * @param b
	 *            byte array to be converted
	 * @return resulting hex string
	 */
	public static String byteArrayToHexString(byte[] b) {
		// check
		if (b == null || b.length == 0) {
			return "";
		}
		
		// init
		StringBuffer sb = new StringBuffer(b.length * 2);

		// convert
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}

		// return
		return (sb.toString().trim().toUpperCase());
	}

	// Convert hex string back to byte array
	public static byte[] hexStringToByteArray(String str) {
		// check input
		if (!str.matches("[0-9A-Fa-f]+"))
			return (null);
		if ((str.length() % 2) != 0)
			return (null);

		// init
		byte[] bytes = new byte[str.length() / 2];

		// convert
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
					16);
		}

		// return
		return (bytes);
	}
}

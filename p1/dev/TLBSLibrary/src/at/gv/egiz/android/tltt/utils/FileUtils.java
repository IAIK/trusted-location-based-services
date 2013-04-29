package at.gv.egiz.android.tltt.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Base64;

/**
 * Offers various methods for file handling.
 * 
 * @author Sandra Kreuzhuber
 * 
 */
public class FileUtils {

	/**
	 * Extracts the file ending of a File.
	 * 
	 * @param path
	 *            to the file
	 * @return a string with the file ending
	 */
	public static String getFileEnding(String path) {
		int dot = path.lastIndexOf(".");
		return path.substring(dot + 1);

	}

	/**
	 * Reads file from local filesystem.
	 * 
	 * @param path
	 *            e.g. mnt/sdcard/test/somefile.xml
	 * @return file as <code>String</code>, decoded with the default encoding
	 *         TODO
	 * @throws IOException
	 */
	public static String readFile(String path) throws IOException {
		if (path.contains("file:///")) {
			path = path.replace("file:///", "");
		}
		// opens a path like this: mnt/sdcard/testy/test7.xml
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			// using the default decoder, TODO: maybe change to some decoder,
			// but we don't use a special encoder right now
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

	/**
	 * Checks if file path ends with specific file extension.
	 * 
	 * @param path
	 * @param ending
	 * @return <code>true</code> if file ending matches, <code>false</code> if
	 *         not
	 */
	public static boolean hasFileEnding(String path, String ending) {
		if (path.endsWith(ending))
			return true;
		return false;
	}

	/**
	 * 
	 * @return a current timestamp e.g. 20120101124530
	 */
	public static String getTimestamp() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyyMMddHHmmss");
		return simpleDateFormat.format(new Date());
	}

	/**
	 * Saves a byte[] to a file.
	 * 
	 * @param array
	 * @param filename
	 * @throws IOException
	 */
	public static File writeByteArrayToFile(byte[] array, String filename)
			throws IOException {

		File file = new File(filename);

		if (file.exists())
			file.delete();

		FileOutputStream fos = new FileOutputStream(file);
		fos.write(array);
		fos.close();

		return file;
	}

	/**
	 * Writes a base64 String to the given file.
	 * 
	 * @param content
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static File writeBase64StringToFile(String content, String filename)
			throws IOException {
		byte[] bytes = Base64.decode(content, Base64.DEFAULT);
		return writeByteArrayToFile(bytes, filename);
	}

	/**
	 * Converts a given file into a byte[].
	 * 
	 * @param path
	 *            e.g. "mnt/sdcard/example.jpg"
	 * @return a byte[] of the given file
	 * @throws IOException
	 */
	public static byte[] getByteArrayFromFile(String path) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(path));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (is.available() > 0) {
			bos.write(is.read());
		}
		return bos.toByteArray();

	}

	/**
	 * Returns the given file as String in Base64 encoding.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String getBase64StringFromFile(String path)
			throws IOException {
		return Base64
				.encodeToString(getByteArrayFromFile(path), Base64.DEFAULT);
	}
}

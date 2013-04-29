package at.gv.egiz.android.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class SDCardAdapter implements StorageAdapter {

	private boolean externalStorageAvailable = false;
	private boolean externalStorageWriteable = false;

	protected Context context;

	public SDCardAdapter(Context context) {
		this.context = context;
		determineState();
	}

	private boolean isWriteAble() {
		return externalStorageWriteable;
	}

	private boolean isReadAble() {
		return externalStorageAvailable || externalStorageWriteable;
	}

	private boolean isAvailable() {
		return externalStorageAvailable;
	}

	private void determineState() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			externalStorageAvailable = externalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			externalStorageAvailable = true;
			externalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			externalStorageAvailable = externalStorageWriteable = false;
		}
	}

	public File[] getFiles(String pathToFolder) {
		determineState();
		if (!isReadAble()) {
			return new File[0];
		}

		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard + pathToFolder);
		if (!(dir.exists())) {
			return new File[0];
		}

		File[] locationClaimFiles = dir.listFiles();
		return locationClaimFiles;
	}

	public boolean write(String serverSignature, String path, String fileName) {
		determineState();
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard + path);
		if (!isWriteAble()) {
			return false;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File locationClaimFile = new File(dir, fileName);
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(locationClaimFile);
			fileOutputStream.write(serverSignature.getBytes());
			fileOutputStream.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String read(String path, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	// public String readFile(String fileName) {
	// determineState();
	// File sdCard = Environment.getExternalStorageDirectory();
	// File dir = new File(sdCard + "/LocSig/");
	// if (!isReadAble()) {
	// return null;
	// }
	// File locationClaimFile = new File(dir, fileName);
	// FileInputStream inputStream;
	// try {
	// inputStream = new FileInputStream(locationClaimFile);
	//
	// ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
	// Util.dumpStream(inputStream, bOutputStream, 1024);
	// return new String(bOutputStream.toByteArray());
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }

}

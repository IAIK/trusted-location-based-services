package at.tugraz.iaik.las.p2.prover;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Environment;

public class TLttManager {
	private static final String tLttParentDirectoryName = "TLTTs";
	
	private String directory;

	public TLttManager(String rootDirectory) {
		this.directory = String.format("%s/%s/%s",
				Environment.getExternalStorageDirectory(),
				rootDirectory,
				TLttManager.tLttParentDirectoryName);
		
		File dir = new File(String.format("%s", this.directory));
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	public String saveTLtt(String fileName, String xmlString) {
		String path = String.format("%s/%s", this.directory,
				fileName);

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write(xmlString.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return path;
	}
}

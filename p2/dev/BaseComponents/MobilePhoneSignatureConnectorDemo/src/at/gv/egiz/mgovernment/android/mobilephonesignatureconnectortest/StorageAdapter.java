package at.gv.egiz.mgovernment.android.mobilephonesignatureconnectortest;

import java.io.File;

public interface StorageAdapter {

	public File[] getFiles(String pathToFolder);

	public boolean write(String fileContent, String path, String fileName);

	public String read(String path, String fileName);

}

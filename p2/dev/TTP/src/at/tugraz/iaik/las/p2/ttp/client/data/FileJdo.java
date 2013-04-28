package at.tugraz.iaik.las.p2.ttp.client.data;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents an uploaded file in the datastore.
 * 
 * @author christian.lesjak@student.tugraz.at
 *
 */
@PersistenceCapable(detachable="true")
public class FileJdo implements IsSerializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
	public String Key;
	
	/**
	 * The file content in bytes.
	 */
	@Persistent
	public byte[] Bytes;
	
	/**
	 * Upload time.
	 */
	@Persistent
	public Date UploadDate;
	
	/**
	 * Filename of the X509 certificate at upload time.
	 */
	@Persistent
	public String SourceFilename;
}

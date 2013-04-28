package at.tugraz.iaik.las.p2.ttp.client.data;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Logs the steps of the protocol and is used to identify sessions.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
@PersistenceCapable(detachable = "true")
public class ProtocolLogJdo implements IsSerializable {

	/**
	 * The key provider that provides the unique ID.
	 */
	@NotPersistent
	public static final ProvidesKey<ProtocolLogJdo> KEY_PROVIDER = new ProvidesKey<ProtocolLogJdo>() {
		public Object getKey(ProtocolLogJdo item) {
			return item == null ? null : item.Key;
		}
	};

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
	public String Key;

	@Persistent
	// Possibility to add some information about client, e.g. IP
	// not possible with hessian afaik
	public String ClientInformation;

	@Persistent
	public Date Timestamp;

	@Persistent
	public String Uid;

	@Persistent
	public String Nonce;

	@Persistent
	public ProtocolStep ProtocolStep;

	@Persistent
	public String StatusMessage;

	@Persistent
	public String Ltt;

	@Persistent
	public Text signedLtt;

	@Persistent
	public Text TLtt;
}
